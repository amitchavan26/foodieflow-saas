# ADR 0001 — Marketplace tenancy model

**Status:** Accepted  
**Date:** 2026-10-15  
**Layer:** 1–2 (locked), used by Layer 3 schema

## Context

FoodieFlow is a food delivery product with four human actors. We needed an explicit answer to: *what is a tenant, what is isolated, and what is global?*

Options considered:

1. **White-label / schema-per-tenant** — one restaurant per logical DB.
2. **Restaurant-owned drivers and customers** — isolation like a franchise POS.
3. **Marketplace with restaurant-as-tenant** — shared customer and driver pools; restaurant commercial data isolated by row.

## Decision

Use **option 3**.

- A **tenant is one restaurant outlet**. Identifier: `restaurants.id` (UUID). Foreign key column name on tenant-owned rows: **`restaurant_id`**.
- **Platform (no `restaurant_id`):** `users` (customers), `delivery_partners` (drivers), `admins`.
- **Tenant-owned (must have `restaurant_id`):** menus, orders, order lines, and all future restaurant staff/payout/ticket tables.
- **Shared-but-sliced:** an order has both `restaurant_id` and `user_id`. Restaurant staff see only their `restaurant_id`. The customer sees only their `user_id`. Drivers see only the assigned job. Admins may cross tenants with an explicit platform context and audit.
- **Physical isolation V1:** one PostgreSQL database, **shared schema**, row-level discriminator. Not schema-per-tenant, not DB-per-tenant.
- **Enforcement (later layers):** JWT `restaurantId` for restaurant roles; persistence filter; tests that cross-tenant reads/writes fail.

On the `restaurants` table itself, **`id` is the tenant key**. Child tables reference `restaurants.id` as `restaurant_id`. We do not duplicate `id` into a second `restaurant_id` column on `restaurants`.

## Consequences

- Discovery, GMV, and admin ops stay simple (cross-tenant reads are platform queries).
- Every restaurant-scoped query **must** include `restaurant_id`.
- Hibernate `@ManyToOne` **across modules is forbidden**; store UUID FKs so tenancy stays a column, not a graph leak.
- Enterprise white-label / schema-per-tenant remains an explicit out-of-scope escape hatch, not a V1 requirement.

## Confirmed product facts

- Customers and drivers are **global / platform** entities.
- Restaurants are the **only** tenants.
- Single-city V1; one restaurant per cart.
