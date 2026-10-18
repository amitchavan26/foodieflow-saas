# Layer 3 — Databases & Storage

**Status:** V1 initial schema  
**Source of truth:** `backend/src/main/resources/db/migration/V1__initial_schema.sql`  
**Tenancy:** [ADR 0001](./decisions/0001-marketplace-tenancy-model.md)

Hibernate `ddl-auto` is **`validate`**. All schema change = new Flyway version.

---

## Entity-relationship diagram

```mermaid
erDiagram
  users {
    uuid id PK
    timestamptz created_at
    timestamptz updated_at
    varchar email UK
    varchar phone UK
    varchar password_hash
    varchar full_name
    varchar status
  }

  delivery_partners {
    uuid id PK
    timestamptz created_at
    timestamptz updated_at
    varchar email UK
    varchar phone UK
    varchar password_hash
    varchar full_name
    varchar status
  }

  admins {
    uuid id PK
    timestamptz created_at
    timestamptz updated_at
    varchar email UK
    varchar password_hash
    varchar full_name
    varchar status
  }

  restaurants {
    uuid id PK
    timestamptz created_at
    timestamptz updated_at
    timestamptz deleted_at
    varchar name
    varchar slug UK
    varchar phone
    varchar address_line
    numeric latitude
    numeric longitude
    varchar status
    boolean paused
    int prep_time_minutes
    int delivery_radius_meters
  }

  menu_categories {
    uuid id PK
    uuid restaurant_id FK
    timestamptz created_at
    timestamptz updated_at
    varchar name
    int sort_order
  }

  menu_items {
    uuid id PK
    uuid restaurant_id FK
    uuid category_id FK
    timestamptz created_at
    timestamptz updated_at
    timestamptz deleted_at
    varchar name
    bigint price_in_cents
    boolean is_available
  }

  orders {
    uuid id PK
    uuid restaurant_id FK
    uuid user_id FK
    uuid delivery_partner_id FK
    timestamptz created_at
    timestamptz updated_at
    varchar status
    varchar payment_status
    varchar payment_provider_ref
    bigint subtotal_in_cents
    bigint delivery_fee_in_cents
    bigint tax_in_cents
    bigint tip_in_cents
    bigint total_in_cents
    char currency
  }

  order_items {
    uuid id PK
    uuid restaurant_id FK
    uuid order_id FK
    uuid menu_item_id FK
    timestamptz created_at
    timestamptz updated_at
    varchar name_snapshot
    bigint unit_price_in_cents
    int quantity
  }

  users ||--o{ orders : "places"
  restaurants ||--o{ menu_categories : "owns"
  restaurants ||--o{ menu_items : "owns"
  restaurants ||--o{ orders : "fulfills"
  restaurants ||--o{ order_items : "tenant slice"
  menu_categories ||--o{ menu_items : "contains"
  menu_items ||--o{ order_items : "snapshotted as"
  orders ||--|{ order_items : "contains"
  delivery_partners ||--o{ orders : "assigned after READY"
```

`admins` has no FKs in V1 (platform operator identity only).

**Tenant key:** `restaurants.id`. Child tenant tables carry `restaurant_id`.  
`order_items.restaurant_id` is denormalized so kitchen/admin queries never scan another tenant’s lines.

---

## Indexes (and why)

| Index | Why |
| --- | --- |
| PK / UNIQUE on `id`, emails, phones, restaurant `slug`, `orders.payment_provider_ref` | Lookups and idempotent PSP refs |
| All FK columns (`restaurant_id`, `user_id`, `category_id`, `order_id`, `menu_item_id`, `delivery_partner_id`) | PostgreSQL does **not** index FKs automatically; joins, `ON DELETE`, and tenant filters would sequential-scan |
| `(restaurant_id, status, created_at DESC)` on `orders` | Restaurant live ticket board (filter tenant + status, sort newest) |
| `(user_id, created_at DESC)` on `orders` | Customer order history |
| `(restaurant_id)` on `menu_categories` / `menu_items` (items also `WHERE deleted_at IS NULL`) | Public menu + tenant CMS; skip tombstoned items |
| `(restaurants.status) WHERE deleted_at IS NULL` | Discovery of live restaurants |
| `(orders.status, created_at)` | Admin / dispatch queues (platform) |

Partial unique on `payment_provider_ref` allows many unpaid rows with `NULL` ref.

---

## What this migration deliberately omits

Payment intents table, webhook inbox, carts, addresses book, restaurant staff, deliveries/GPS, notification outbox, opening-hours rows. Columns on `orders` cover pay-then-confirm and post-READY driver FK until those modules get their own tables (later layers / later Flyway versions).
