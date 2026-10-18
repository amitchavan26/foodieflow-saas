# System Design — Food Delivery SaaS (Layer 1)

**Status:** Draft for Layer 1 (System Design)  
**Stack (locked):** Spring Boot 3 backend, Next.js frontend  
**Architecture style (locked):** Modular monolith  
**Stage:** V1 foundation — no application code yet

> `CLAUDE.md` in this repo is currently empty. This document is the Layer 1 source of truth until project rules are filled in. It follows the 15-Layer Playbook: lock problem, actors, flows, requirements, tenancy, and V1 cuts **before** architecture implementation.

---

## 1. Problem statement

Build a **multi-tenant Food Delivery SaaS** that lets a platform operator run an online marketplace where:

- **Customers** discover nearby restaurants, place orders, pay, and track delivery.
- **Restaurants** manage menus, accept/prepare orders, and fulfill them.
- **Drivers** pick up and deliver orders with live status.
- **Admins** operate the platform (onboarding, catalog quality, commissions, disputes).

V1 is a **single-city, web-first marketplace** with restaurant tenancy isolation, not a set of microservices and not a white-label “one restaurant per deployment” product.

**Success for V1:** a customer can complete browse → cart → pay → restaurant accept → driver assign → delivered, with restaurant data never leaking across tenants, under the NFRs below.

---

## 2. Actors

| Actor | Who they are | Primary job |
| --- | --- | --- |
| Customer | End consumer on the platform | Find food, order, pay, track |
| Restaurant staff | Tenant users (owner / manager / kitchen) | Menu, hours, accept/reject, prep status |
| Driver | Platform delivery partner | Accept assignment, pickup, deliver |
| Admin | Platform operator | Onboard tenants, config, disputes, ops |

Supporting (system) actors: **Payment provider**, **SMS/email/push gateway**, **Maps / geocoding** (consumed via adapters; not in-process modules).

---

## 3. Core user flows

### 3.1 Customer

1. **Register / login** — email or phone OTP; session JWT.
2. **Set delivery location** — pin or saved address; all discovery is geo-scoped.
3. **Discover restaurants** — nearby open restaurants, cuisine/search, ETA and fee estimates.
4. **View menu** — categories, items, variants, availability, photos.
5. **Cart** — quantities, item notes, restaurant lock (one restaurant per cart in V1).
6. **Checkout** — address, tip (optional), promo (optional, V1: platform coupons only).
7. **Pay** — authorization/capture via payment provider; order is confirmed only after payment success (or COD if enabled — see assumptions).
8. **Track** — order states: `PLACED → RESTAURANT_ACCEPTED → PREPARING → READY → PICKED_UP → DELIVERED` (plus `CANCELLED` / `REJECTED`).
9. **History & support** — past orders, simple cancel-before-accept, rate restaurant/driver after delivery.

### 3.2 Restaurant

1. **Onboard** — admin-approved tenant; owner creates staff users.
2. **Configure storefront** — hours, prep time, delivery radius (platform-enforced max), pause/resume.
3. **Manage menu** — categories, items, prices, stock flags, 86 an item.
4. **Receive orders** — new-order alert; accept or reject with reason within SLA.
5. **Fulfill** — mark preparing / ready for pickup.
6. **View today’s board** — live tickets and basic sales for *this tenant only*.

### 3.3 Driver

1. **Go online** in a service zone.
2. **Receive assignment** (platform dispatch — V1: nearest available driver, not auction).
3. **Accept / decline** (limited declines; too many → cooldown).
4. **Navigate to restaurant** → confirm pickup.
5. **Navigate to customer** → confirm drop-off (OTP or tap-to-complete).
6. **Heartbeat location** while on a job (coarse interval when idle).

### 3.4 Admin

1. **Approve restaurant tenants** and commission plan.
2. **City / zone config** — service polygon, fees, peak flags.
3. **Catalog quality** — disable restaurants, force-pause, take down items.
4. **Order ops** — search any order, force-cancel with refund path, reassign driver.
5. **User ops** — suspend customer/driver accounts.
6. **Read-only finance snapshot** — GMV, platform fee, payouts *queued* (payout execution may be manual in V1).

### 3.5 Happy-path sequence (platform)

```text
Customer checkout
  → Payment authorized
  → Order created (Order module)
  → Restaurant notified
  → Restaurant accepts
  → Kitchen prepares → ready
  → Dispatcher assigns driver
  → Pickup → drop-off
  → Payment captured / settled flag
  → Notifications at each transition
```

---

## 4. Functional requirements (V1)

### 4.1 Identity & access

- Role-based access: `CUSTOMER`, `RESTAURANT_OWNER`, `RESTAURANT_STAFF`, `DRIVER`, `ADMIN`.
- Restaurant staff always act **inside one tenant**.
- Admin may cross tenants; every admin action is auditable.

### 4.2 Catalog & discovery

- Geo search: restaurants within delivery radius that are currently open.
- Filter by cuisine / veg / rating (rating can be a stored aggregate, not a live compute).
- Menu reads are tenant-scoped; public catalog is a **controlled projection** of tenant data.

### 4.3 Cart & order

- One active cart per customer; one restaurant per cart.
- Server-side price snapshot at place-order time (menu price changes must not mutate placed orders).
- Idempotent place-order (client `Idempotency-Key`).
- Cancel: customer may cancel until restaurant accept; after that, admin/restaurant policy.

### 4.4 Payment

- Integrate one PSP (see assumptions).
- States: `PENDING → AUTHORIZED → CAPTURED | FAILED | REFUNDED`.
- Webhooks are the source of truth for PSP state; app APIs never trust client “payment success”.
- Store PSP references only; **no raw card data**.

### 4.5 Delivery

- Assignment after `READY` (or earlier if restaurant opts into “assign while preparing” — V1 default: after `READY`).
- Driver location updates for the assigned order’s tracking channel only.
- Completion confirmation required to close the order.

### 4.6 Notifications

- In-app + email or SMS for: order placed, accepted, rejected, driver assigned, nearby, delivered, refunded.
- At-least-once delivery is acceptable; templates are tenant-branded with platform footer.

---

## 5. Non-functional requirements

These NFRs are first-class (from the Food Delivery system-design notes). They drive Layer 2 choices: cache, async events, connection pooling, and later horizontal scale of the **same** monolith.

### 5.1 Latency

| Path | Target |
| --- | --- |
| Interactive API (browse, menu, cart, place order ack) | **p95 &lt; 2s** end-to-end including network to our origin |
| Authenticated “hot” reads (menu, restaurant list with cache warm) | **p95 &lt; 300ms** at the application |
| Place-order + payment redirect/confirm handshake | **p95 &lt; 2s** for *our* work; PSP time is excluded but must be async/webhook-driven |
| Driver location ingest | Accept and enqueue **&lt; 100ms** app-side; fan-out to trackers is async |

**Design implication:** no synchronous cross-module chatty loops; cache restaurant list/menu; payment and notifications off the request thread after persist.

### 5.2 Availability

- **99.9% monthly uptime** for customer browse + order + pay APIs (~43 minutes downtime/month budget).
- Degraded modes (must be designed, not improvised):
  - Search cache stale-OK if DB replica lag.
  - If dispatcher is unhealthy: restaurant can still accept; assignment retries.
  - If notification provider is down: order still proceeds; retry queue.
- Payment webhook processing must survive process restart (persistent inbox).

### 5.3 Scale (high traffic)

V1 **capacity envelope** (planning numbers, not a promise of day-one load):

| Metric | Planning target |
| --- | --- |
| Concurrent customers (peak dinner) | 10,000 |
| Orders per minute (peak) | 200 |
| Driver location pings | 20/s sustained, 100/s peak |
| Catalog read:write | ~100:1 |

**Scale strategy (monolith, not microservices):**

1. Stateless app instances behind a load balancer (session in JWT / Redis).
2. PostgreSQL primary + read replica for catalog/discovery.
3. Redis: session/rate-limit, menu/restaurant cache, geo radius if needed.
4. Vertical then horizontal scale of the **same** deployable.
5. Split a module out **only** when a measured bottleneck or team/compliance constraint appears.

### 5.4 Consistency

| Data | Model |
| --- | --- |
| Payments, order totals, stock flags at checkout | **Strong** (single DB transaction in Order + Payment records) |
| Menu, hours, restaurant pause | **Read-your-writes** for the tenant; customers may see cache ≤ 60s |
| Driver location | **Eventual** (last ping wins) |
| Notifications | **Eventual**, at-least-once |

### 5.5 Security & tenancy (NFR)

- TLS everywhere.
- Tenant isolation is a **security NFR**, not a feature (see §6).
- Secrets in env / secret manager, never in git.
- PSP webhooks authenticated (signature).
- Rate limits on login, OTP, place-order, location ping.

### 5.6 Observability (needed to *prove* 99.9% and &lt;2s)

- Structured logs with `traceId`, `tenantId`, `orderId`.
- Golden signals: latency, traffic, errors, saturation.
- Uptime check on `/health` and synthetic “list restaurants”.

---

## 6. Explicit tenancy model

### 6.1 What a tenant is

A **tenant = one Restaurant (legal/storefront entity)** with a stable `tenant_id` (UUID).  
A restaurant group with multiple outlets is **multiple tenants** in V1 (no parent-org hierarchy).

The **platform** is not a tenant; Admin operates in a **platform context** (`tenant_id = null`, `role = ADMIN`).

### 6.2 Isolation style (V1): shared database, shared schema, discriminator column

| Option | V1 choice |
| --- | --- |
| Database | **One PostgreSQL**, shared schema |
| Isolation | **Row-level**: every restaurant-owned row has `tenant_id NOT NULL` |
| Enforcement | Application + persistence filters (mandatory) |
| Future escape hatch | Schema-per-tenant or DB-per-tenant for enterprise white-label — **out of V1** |

**Why this model:** marketplace queries (nearby restaurants, platform GMV) are cross-tenant by nature; schema-per-tenant makes discovery and admin ops expensive. Isolation is still strict for **writes and tenant reads**.

### 6.3 Data classification

| Class | Examples | Isolation rule |
| --- | --- | --- |
| **Tenant-owned** | Menu, hours, restaurant staff, restaurant payout account refs, restaurant order tickets | Always filtered by `tenant_id`. Staff JWTs carry `tenant_id`. |
| **Marketplace-shared** | Customers, addresses, drivers, zones, platform coupons | No restaurant `tenant_id`. Restaurants **must not** query other customers except those on *their* orders. |
| **Shared-but-sliced** | Orders, payments, deliveries | Row has `tenant_id` (fulfilling restaurant) **and** `customer_id`. Restaurant sees only `tenant_id = me`. Customer sees only `customer_id = me`. Driver sees only assigned delivery. Admin sees all. |
| **Platform-owned** | Admin users, commissions config, audit log | Admin only. |

### 6.4 Enforcement layers (defense in depth)

1. **JWT / security context** — `userId`, `roles`, `tenantId` (required for restaurant roles).
2. **API authorization** — restaurant controllers refuse missing/mismatched `tenantId`.
3. **Persistence interceptor / Hibernate filter** — all tenant-owned entities auto-filter `tenant_id`.
4. **Tests** — cross-tenant read/write attempts must fail (Layer 2 will mandate this in module tests).
5. **Admin break-glass** — explicit `PlatformContext`; every query tagged in audit log.

**Hard rule:** a module may not load an entity by primary key alone if that entity is tenant-owned; lookup is `(id, tenant_id)` or filter-on.

### 6.5 What is *not* isolated

- Global customer identity (one customer account works across restaurants).
- Global driver fleet (drivers are platform resources in V1).
- Public discovery index (derived from tenant catalogs).

---

## 7. Out of scope for V1

Do **not** design, document as required, or implement these until a later layer explicitly pulls them in:

- Native iOS/Android apps (Next.js responsive web only).
- Microservices split, Kubernetes-per-module, or message broker as the *primary* inter-module bus.
- Multi-region active-active, multi-city launch, or per-country compliance packs.
- White-label / schema-per-tenant / custom domains per restaurant.
- Restaurant groups, franchisor hierarchy, multi-outlet manager in one login.
- Grocery, dark stores, scheduled orders, group orders, dine-in, pickup-only marketplace mode (pickup *flag* may exist; not a separate product).
- Driver auction / surge pricing / ML dispatch / batching multiple orders per rider.
- In-app chat, live video, call masking.
- Loyalty, wallets (beyond PSP), subscriptions, corporate accounts.
- Real-time kitchen display hardware / POS injection.
- Automated weekly payouts and tax invoicing packs (queue + export is enough).
- Multi-PSP routing, saved cards vault UI beyond what the PSP-hosted page provides.
- Advanced search (typo-tolerant Elasticsearch) — PostgreSQL + Redis is V1.
- Customer-to-customer social, reviews with photos/moderation queue (simple 1–5 rating only).

---

## 8. Constraints for later layers

- **No Java/Kotlin production code or build files until Layer 1–2 docs are accepted.**
- One Spring Boot 3 deployable; Next.js talks to it over HTTPS JSON APIs.
- Inter-module communication in-process (see `docs/architecture.md`).
- New features land in an existing module or a *named* new module — never in a generic `util` / `services` dump.
- NFRs in §5 are acceptance criteria for architecture, not slogans.

---

## 9. Open items owned by the product owner

See the three domain assumptions listed in the architecture handoff (and the chat close-out). Until confirmed, the defaults in those assumptions stand.
