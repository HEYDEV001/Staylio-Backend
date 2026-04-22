# Staylio-Backend

> A robust, production-ready backend for a hotel & property booking platform — inspired by Airbnb — built with Java Spring Boot.

---

## Project Objective

Staylio-Backend is designed to power a full-scale hotel and short-term rental marketplace. The goal is to provide a scalable, secure, and maintainable RESTful backend that handles everything from property listings and room management to dynamic pricing, booking lifecycle, and guest reviews — all while enforcing role-based access and secure authentication.

The project demonstrates real-world backend engineering practices, including design patterns, LLD-driven optimisations, clean layered architecture, efficient database query design, and containerized deployment using Docker.

---

## Key Features

- **Property & Room Management** — Full CRUD for hotels, rooms, and amenities with support for multiple room types per property.
- **Dynamic Pricing Engine** — Automated price calculation per room per day based on configurable strategies (occupancy-based, seasonal, surge pricing). Prices are pre-computed and stored efficiently to avoid expensive on-the-fly recalculation.
- **Booking Lifecycle Management** — End-to-end booking flow covering availability checks, reservation, confirmation, and cancellation with proper state transitions.
- **Role-Based Access Control (RBAC)** — Separate roles for guests, hotel managers, and admins, enforced via Spring Security.
- **JWT Authentication** — Stateless token-based authentication for secure, scalable API access.
- **DTO Layer** — Clean separation between persistence entities and API contracts using Data Transfer Objects.
- **Global Exception Handling** — Consistent error responses across the application via a centralised exception handler.
- **Strategy Pattern for Pricing** — Pluggable pricing strategies decoupled from the core booking service, making it easy to add or swap pricing logic.
- **Dockerized Deployment** — Fully containerised with Docker and Docker Compose for consistent, one-command setup across environments.

---

## Project Structure

```
Staylio-Backend/
│
├── AirBnbApplication.java          # Spring Boot entry point
│
├── advice/                         # Global exception handling (@RestControllerAdvice)
│
├── config/                         # Application configuration (Security config, beans, etc.)
│
├── controller/                     # REST API controllers (request handling & routing)
│
├── dto/                            # Data Transfer Objects (request/response models)
│
├── entities/                       # JPA entity classes (database models)
│
├── exceptions/                     # Custom exception classes
│
├── repository/                     # Spring Data JPA repositories (DB queries)
│
├── security/                       # JWT filters, token utilities, UserDetailsService
│
├── service/                        # Business logic layer (service interfaces & implementations)
│
├── strategy/                       # Pricing strategy implementations (Strategy Design Pattern)
│
└── util/                           # Utility/helper classes
```

### Layer Responsibilities

| Layer | Responsibility |
|---|---|
| `controller` | Accepts HTTP requests, delegates to service, returns responses |
| `service` | Core business logic, orchestrates repositories and strategies |
| `repository` | Database interactions via Spring Data JPA |
| `entities` | JPA-mapped domain objects |
| `dto` | Decoupled request/response contracts |
| `strategy` | Swappable pricing algorithm implementations |
| `security` | JWT generation, validation, and filter chain configuration |
| `advice` | Centralised error handling and exception-to-HTTP mapping |
| `exceptions` | Custom typed exceptions for domain-specific error cases |
| `config` | Spring beans, security config, and app-level settings |
| `util` | Reusable helper methods and constants |

---

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 17+ | Core programming language |
| Spring Boot | Application framework |
| Spring Security | Authentication & authorisation |
| JWT (JSON Web Tokens) | Stateless session management |
| Spring Data JPA / Hibernate | ORM and database abstraction |
| PostgreSQL | Relational database |
| Docker + Docker Compose | Containerisation & service orchestration |
| Maven | Build and dependency management |
| Lombok | Boilerplate reduction |

---

## Challenges & Optimisations

### Challenge 1 — Scalable Dynamic Pricing Over Long Date Ranges

**The Problem:**

One of the most significant challenges was implementing dynamic pricing for hotel rooms. When a user searches for availability or an admin configures pricing, the system needs to calculate the price for every single date in the requested range. For a long range such as 3 months or more, this meant the system had to compute and potentially update prices for 90+ records per room. With multiple rooms per hotel and multiple hotels, a naive implementation would result in thousands of redundant calculations hitting the database repeatedly — causing severe performance degradation, query timeouts, and an unacceptably slow user experience.

**The Solution — LLD-Driven Query Optimisation:**

Rather than calculating and updating dynamic prices row-by-row in a loop (the N+1 anti-pattern), the solution applied Low-Level Design (LLD) principles to restructure the entire computation and persistence strategy:

1. **Batch Computation Instead of Iterative Updates** — Pricing for a date range is computed in bulk as a single operation rather than iterating day-by-day with individual database writes. The pricing logic aggregates all dates for a range, applies the relevant strategy, and prepares a batch of records in memory before a single batched write to the database.

2. **Strategy Pattern for Pricing Logic** — Each pricing algorithm (e.g., base pricing, occupancy-based surge, seasonal multipliers) is encapsulated as a separate class implementing a common `PricingStrategy` interface (housed in the `strategy/` package). This allows the service layer to apply the correct strategy without tight coupling and makes it trivial to add new pricing models without modifying existing code — adhering to the Open/Closed Principle.

3. **Pre-computation & Storage** — Instead of computing prices on every booking query, prices are pre-calculated and persisted for future dates. This shifts the computational cost to the background/admin configuration phase, making real-time availability queries fast and lightweight.

4. **Optimised Repository Queries** — Custom JPQL/native queries in the `repository/` layer were written to fetch and upsert price records for entire date ranges in one round-trip, drastically reducing database calls compared to individual per-day queries.

**Result:** The system can now handle dynamic pricing configuration for periods of 3 months, 6 months, or more without performance issues, while keeping the booking query path fast and efficient for end users.

---

### Challenge 2 — Secure Multi-Role Access Control

**The Problem:** Managing what guests, hotel managers, and admins can each access and modify required careful security design to prevent privilege escalation and unauthorised data access.

**The Solution:** Spring Security with JWT was used to implement stateless RBAC. Custom filters validate tokens on every request, and method-level security annotations enforce role checks at the service layer — ensuring that, for example, a guest cannot modify another user's booking or access hotel management endpoints.

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- **PostgreSQL** running locally — OR — **Docker & Docker Compose** (recommended, no manual DB setup needed)

### Option 1 — Running Locally (without Docker)

```bash
# Clone the repository
git clone https://github.com/HEYDEV001/Staylio-Backend.git
cd Staylio-Backend

# Configure your database credentials in
# src/main/resources/application.properties or application.yml

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The server will start on `http://localhost:8080` by default.

### Environment Configuration

Update your `application.properties` with:

```properties
spring.datasource.url=jdbc:postgresql://staylio-db:5432/staylioDB
spring.datasource.username=your_username
spring.datasource.password=your_password

jwt.secret=your_jwt_secret_key

frontend.url=http://localhost:8080

stripe.secret.key=your_stripe_secret_key

stripe.webhook.secret=your_stripe_webhook_secret
```

---

### Option 2 — Running with Docker & Docker Compose (Recommended)

The project is fully Dockerized. Docker Compose spins up both the Spring Boot application and a **PostgreSQL** database together — no manual database installation or configuration required.

#### 1. Clone the repository

```bash
git clone https://github.com/HEYDEV001/Staylio-Backend.git
cd Staylio-Backend
```

#### 2. Build and start all services

```bash
docker-compose up --build
```

This single command will:
- Build the Spring Boot application Docker image
- Pull the official PostgreSQL image
- Start both containers with correct networking and environment variables wired up
- Apply schema on startup via Hibernate DDL

#### 3. Access the application

```
http://localhost:8080
```

#### 4. Stop the services

```bash
# Stop containers (preserves database data)
docker-compose down

# Stop and remove all containers + volumes (resets database)
docker-compose down -v
```

#### Docker Compose Services Summary

| Service | Description | Port |
|---|---|---|
| `app` | Spring Boot backend application | `8080` |
| `db` | PostgreSQL database | `5432` |

#### Useful Docker Commands

```bash
# View running containers
docker ps

# Stream application logs
docker-compose logs -f app

# Stream database logs
docker-compose logs -f db

# Rebuild only the app after code changes
docker-compose up --build app

# Open a PostgreSQL shell inside the DB container
docker exec -it <db_container_name> psql -U your_db_user -d staylio
```

---

## API Overview

| Module | Base Path | Description |
|---|---|---|
| Auth | `/auth` | Register, login, token refresh |
| Hotels | `/hotels` | List, search, create, update hotels |
| Rooms | `/rooms` | Room management per hotel |
| Bookings | `/bookings` | Create, view, cancel bookings |
| Users | `/users` | Profile management |

> Full API documentation (Postman collection / Swagger) can be added as a future enhancement.

## Author

**HEYDEV001** — [GitHub Profile](https://github.com/HEYDEV001)

---

> *Staylio-Backend is a portfolio/learning project built to demonstrate real-world Spring Boot backend engineering, design patterns, and system optimisation techniques.*
