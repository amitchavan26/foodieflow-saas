# CLAUDE.md — FoodieFlow (Food Delivery SaaS)

This file is **project memory**. Agents must read it before writing code.  
Human source of truth for product/architecture: `docs/system-design.md`, `docs/architecture.md`, `docs/decisions/`.

**Current layer:** Layer 3 (Databases & Storage) complete. Next: Layer 4 (API contracts). Layers 1–2 are accepted.

---

## What this product does

A **single marketplace** food delivery platform (not white-label). Customers search restaurants, pay, and track deliveries. Restaurants (tenants) manage menus and fulfill orders. Drivers are platform-wide delivery partners. Admins operate the city/platform.

V1: **one city**, **web-first** (Next.js), **pay-then-confirm**, **no COD**, driver assigned **only after READY**.

---

## Stack (DO NOT CHANGE WITHOUT ASKING)

| Layer | Choice |
| --- | --- |
| Product name | FoodieFlow |
| Frontend | Next.js 14+ (App Router) + TypeScript + Tailwind CSS |
| Backend | Java 21 + Spring Boot 3.x REST API |
| Build | Gradle (Kotlin DSL), module `backend/` |
| Persistence | PostgreSQL + Spring Data JPA / Hibernate |
| Migrations | **Flyway only** — never edit schema by hand, never `ddl-auto=update` |
| Auth (later layer) | Spring Security + JWT |
| Jobs | Spring `@Async` / Redis (later) |
| Java root package | `com.fooddelivery` |
| Artifact | `food-delivery-api` (one deployable) |

**NEVER** suggest Node/Express backends, Prisma, Python, or a microservice split for V1.

---

## Architecture decisions (locked)

1. **Modular monolith** — six modules: `user`, `restaurant`, `order`, `payment`, `delivery`, `notification` + `common`. One JVM, one PostgreSQL, optional Redis.
2. **Package-by-feature** — `com.fooddelivery.<module>.{api,application,domain,infrastructure}`. Not package-by-layer at the top level.
3. **Inter-module communication** — Spring `ApplicationEventPublisher` + `@TransactionalEventListener(AFTER_COMMIT)` for domain events (`OrderPlacedEvent`, …). Narrow **query ports** for in-request reads. No Kafka/Rabbit as the V1 backbone. No JPA entity sharing across modules (FK columns as UUID, not `@ManyToOne` across modules).
4. **Tenancy** — restaurant is the tenant. Column name **`restaurant_id`**. Customers, drivers, admins are **platform** rows (no `restaurant_id`). See ADR 0001.
5. **Money** — integer **minor units** (`*_in_cents BIGINT`). Never `float`/`double`/`numeric` for currency.
6. **IDs / audit** — every table: `id UUID`, `created_at`, `updated_at`. Soft delete (`deleted_at`) on `restaurants` and `menu_items` only (V1).

ADRs:

- `docs/decisions/0001-marketplace-tenancy-model.md`
- `docs/decisions/0002-modular-monolith-communication.md`

---

## Confirmed assumptions (do not reopen)

1. **Marketplace, not white-label.** One platform. Customers and drivers are global. Restaurants are tenants. Shared driver fleet. No schema-per-tenant in V1.
2. **Pay-then-confirm.** PSP authorization before the restaurant sees a firm order. **No COD in V1.**
3. **Single-city V1.** One restaurant per cart. Dispatch **only after** restaurant marks the order `READY`. Nearest-available driver. No surge, auction, or batching.

---

## 15-Layer Playbook (strict rules)

Execute **in order**. Do not skip ahead. Do not “helpfully” implement the next layer.

| Layer | Name | Status |
| --- | --- | --- |
| 1 | System Design | Done — `docs/system-design.md` |
| 2 | System Architecture | Done — `docs/architecture.md` |
| 3 | Databases & Storage | This layer — Flyway + JPA entities only |
| 4 | API contracts | Not started |
| 5 | AuthN / AuthZ | Not started |
| 6 | Domain logic / application services | Not started |
| 7 | Frontend (Next.js) | Not started |
| 8 | External integrations (PSP, maps, SMS) | Not started |
| 9 | Background jobs & outbox | Not started |
| 10 | Caching & real-time tracking | Not started |
| 11 | Observability | Not started |
| 12 | Automated tests (ArchUnit, tenancy, integration) | Not started |
| 13 | Security hardening (rate limits, secrets, webhook verify) | Not started |
| 14 | CI/CD & deploy | Not started |
| 15 | Launch / runbooks / NFR proof (p95 &lt; 2s, 99.9%) | Not started |

**Playbook laws**

- **Plan first, code second.** If docs and ADRs contradict a prompt, stop and ask.
- **One layer’s artifacts only.** Layer 3 = schema + entities + migrations. No services, controllers, or DTO APIs until those layers.
- **Small, verifiable diffs.** New code belongs in an existing module (or a named new module + doc update). Never a `util` / `services` dumping ground.
- **Flyway is the schema source of truth.** `V{n}__{name}.sql`. `spring.jpa.hibernate.ddl-auto=validate`.
- **Every tenant query filters `restaurant_id`.** Admin is the only cross-tenant actor and must be explicit.
- **Every future endpoint** (Layer 4+): `@Valid` DTO, authentication, authorization, tests. Do not skip tests.
- **No secrets in git.** Environment variables only.
- **Don’t Fool Me:** before finishing a layer, list stubs, type assumptions, and confirm tests were not skipped (or that the layer forbade tests).
- **NFRs are constraints:** p95 &lt; 2s interactive APIs, 99.9% uptime, peak-dinner scale via the same monolith (cache/replicas first).

---

## Where new code goes

| Concern | Package |
| --- | --- |
| Shared ID/audit | `com.fooddelivery.common.persistence` |
| Customers | `com.fooddelivery.user` |
| Admins | `com.fooddelivery.user` (platform; not a tenant) |
| Restaurants, menus | `com.fooddelivery.restaurant` |
| Cart/orders | `com.fooddelivery.order` |
| PSP/webhooks | `com.fooddelivery.payment` |
| Drivers/dispatch | `com.fooddelivery.delivery` |
| Email/SMS/push | `com.fooddelivery.notification` |

Frontend (later): `frontend/src/app/(customer|restaurant|driver|admin)/`.
