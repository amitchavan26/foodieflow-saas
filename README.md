# 🍕 FoodieFlow

**A location-aware food delivery platform with secure authentication, geospatial restaurant discovery, and real payment processing.**

[![Java](https://img.shields.io/badge/Java-21-orange)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-green)]()
[![Next.js](https://img.shields.io/badge/Next.js-16-black)]()
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791)]()
[![PostGIS](https://img.shields.io/badge/PostGIS-3.3-green)]()
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED)]()

## 🔗 Live Demo

- **Frontend:** https://foodieflow-frontend.onrender.com
- **Backend API:** https://foodieflow-saas.onrender.com/api/v1

> The free-tier services spin down when idle. The first request may take 30–60 seconds to warm up.

## ✨ Features

- **Geospatial discovery** — find restaurants within a radius using PostGIS spatial queries.
- **JWT authentication** — registration, login, and role-based authorization (Customer / Restaurant / Admin).
- **Menu & cart flow** — browse available items, build a cart, and submit a delivery order.
- **Payment checkout** — server-created Razorpay orders + official Razorpay Checkout (test mode).
- **Reliable persistence** — PostgreSQL with Flyway migrations and seeded demo data.
- **Background processing** — Spring application events with async, retry-backed order handling.
- **Containerized delivery** — multi-stage Docker builds for both services.
- **Cloud deployment** — deployed on Render with a managed PostgreSQL database.

## 🏗️ Architecture

Modular monolith — one deployable backend, organized by business domain.

             ┌──────────────────────────┐
             │   Next.js (App Router)   │
             │  Cart / Auth / Checkout  │
             └────────────┬─────────────┘
                          │ HTTPS + JWT
             ┌────────────▼─────────────┐
             │      Spring Boot API     │
             │  user / restaurant /     │
             │  order / payment         │
             └─────┬──────────────┬─────┘
                   │              │
                   │              │
          ┌────────▼──────┐  ┌────▼─────────────┐
          │ PostgreSQL    │  │ Razorpay API     │
          │ + PostGIS     │  │ (test mode)      │
          │ + Flyway      │  └──────────────────┘
          └───────────────┘


### Why a modular monolith?
It keeps transactional boundaries simple while the domain packages (`user`, `restaurant`, `order`, `payment`) stay decoupled, so services can be extracted later without rewriting business logic.

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Next.js, React, TypeScript, Tailwind CSS |
| Backend | Java 21, Spring Boot, Spring Security |
| Database | PostgreSQL 15, PostGIS, Hibernate/JPA |
| Migrations | Flyway |
| Payments | Razorpay (Java SDK + Checkout.js) |
| Auth | JWT, BCrypt password hashing |
| Delivery | Docker, Render, GitHub Actions |

## 📁 Project Structure
foodieflow-saas/
├── backend/
│ ├── src/main/java/com/fooddelivery/
│ │ ├── common/ # security, config, shared utilities
│ │ ├── user/ # domain, API, persistence
│ │ ├── restaurant/ # menu + geospatial search
│ │ ├── order/ # order lifecycle + events
│ │ └── payment/ # provider integration
│ ├── src/main/resources/db/migration/
│ ├── Dockerfile
│ └── build.gradle.kts
├── frontend/
│ ├── src/app/ # App Router pages
│ ├── src/components/ # Cart, Checkout, Auth UI
│ ├── src/context/ # Cart state
│ ├── src/lib/api.ts # typed API client
│ └── Dockerfile
└── .github/workflows/


## 🔌 Core Endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/auth/register` | Create an account |
| POST | `/api/v1/auth/login` | Obtain a JWT |
| GET | `/api/v1/restaurants?lat=&lng=&radiusMeters=` | Nearby restaurants |
| POST | `/api/v1/restaurants/{id}/orders` | Create order + payment session |