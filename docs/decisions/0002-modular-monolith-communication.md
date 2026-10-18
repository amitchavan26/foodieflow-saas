# ADR 0002 — Modular monolith and inter-module communication

**Status:** Accepted  
**Date:** 2026-10-15  
**Layer:** 2 (locked)

## Context

We need module boundaries that an AI agent can obey for months without producing a distributed system or a big ball of mud.

Options considered:

1. **Microservices** from day one (User, Order, Payment, … as separate processes).
2. **Classic layered monolith** (`controller`, `service`, `repository` at the top level).
3. **Modular monolith, package-by-feature**, in-process events + narrow query ports.

## Decision

Use **option 3**.

- **One** Spring Boot 3 deployable: `food-delivery-api`.
- **Package-by-feature** under `com.fooddelivery.{user,restaurant,order,payment,delivery,notification,common}`.
- Each feature module’s internal layers: `api` → `application` → `domain` ← `infrastructure`.
- **Writes** stay in the owning module (see `docs/architecture.md` ownership table).
- **Side effects** after a successful commit: Spring **`ApplicationEventPublisher`** and **`@TransactionalEventListener(phase = AFTER_COMMIT)`**. Canonical example: `OrderPlacedEvent`.
- **In-request reads** of another module’s data: query **ports** returning immutable DTOs, never JPA entities.
- **Forbidden in V1:** HTTP between modules, shared JPA entities across modules, Kafka/Rabbit as the primary bus, a generic `services`/`util` package for business logic.

Reliability upgrade path (same event names): transactional **outbox** when process crash between commit and listener is unacceptable (especially money). Payment webhooks use an **inbox** (later layer).

## Consequences

- We can meet p95 &lt; 2s and 99.9% uptime by scaling **stateless instances** of the same app.
- ArchUnit (Layer 12) will fail the build on illegal package imports.
- Extracting Delivery or Payment later is a deployment change, not a rename of events.
- Developers/agents must not inject another module’s repository.

## Related risks (from Layer 2)

1. Boundary decay → ArchUnit + no cross-module entities.  
2. Order/Payment dual write → idempotency, inbox, state machine (later layers).  
3. Tracking vs checkout saturation → Redis for pings; do not block place-order on Maps/SMS.
