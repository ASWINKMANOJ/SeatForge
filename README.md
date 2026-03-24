# SeatForge

**High-Throughput, Horizontally Scalable Event Booking Backend**

---

## Overview

**SeatForge** is a horizontally scalable backend service for event ticket booking, designed to handle **high request throughput (10,000+ RPS)** with predictable latency.

The system is built with **Spring Boot 4 (Java 21)**, secured via **Auth0 (OAuth2 / JWT)**, backed by **PostgreSQL** and **Redis**, containerized with **Docker**, and load-balanced using **NGINX** locally or **AWS ALB** in production.  
It is fully stateless and built for **horizontal scaling**.

---

## Key Goals

* Handle **10k+ RPS** reliably
* Maintain **low and predictable latency**
* Support **horizontal scaling** without configuration changes
* Be cloud-ready (AWS ECS / ALB)
* Secure all write and user-specific operations via Auth0 JWT
* Provide automated event lifecycle management (status transitions, seat locking)

---

## Architecture

### Local / Development

```
Client
  ↓
NGINX (Reverse Proxy + Load Balancer)
  ↓
seat-service Containers (N replicas)
  ↓
PostgreSQL + Redis
```

### Cloud / Production (AWS)

```
Internet
  ↓
AWS Application Load Balancer (ALB)
  ↓
ECS Service (Spring Boot Tasks, Auto-Scaled)
  ↓
RDS PostgreSQL + ElastiCache Redis
```

> In AWS, **ALB replaces NGINX**. The application itself remains unchanged.

---

## Technology Stack

| Layer            | Technology                          |
| ---------------- | ----------------------------------- |
| Language         | Java 21                             |
| Framework        | Spring Boot 4                       |
| Build Tool       | Gradle                              |
| Auth             | Auth0 (OAuth2 JWT Resource Server)  |
| ORM              | Spring Data JPA (Hibernate)         |
| Database         | PostgreSQL                          |
| Caching / Locks  | Redis (Redisson + Spring Cache)     |
| Containerization | Docker                              |
| Local LB         | NGINX                               |
| Cloud Runtime    | AWS ECS (Fargate)                   |
| Cloud LB         | AWS ALB                             |
| Load Testing     | Autocannon                          |

---

## Project Structure

```
SeatForge/
│
├── docker-compose.yml          # Orchestrates seat-service, NGINX, PostgreSQL, Redis
│
├── nginx/
│   └── nginx.conf              # Local reverse proxy + round-robin load balancer
│
└── seat_service/
    ├── Dockerfile              # eclipse-temurin:21-jre-alpine runtime image
    ├── build.gradle
    ├── settings.gradle
    └── src/main/java/com/example/seat_service/
        ├── config/
        │   ├── RedisConfig.java
        │   └── security/
        │       ├── SecurityConfig.java         # JWT filter chain + RBAC rules
        │       ├── Auth0JwtConverter.java      # Maps Auth0 permissions to GrantedAuthority
        │       └── AudienceValidator.java
        ├── controllers/
        │   ├── EventController.java
        │   ├── BookingController.java
        │   ├── SeatController.java
        │   ├── VenueController.java
        │   ├── CityController.java
        │   └── AdminController.java
        ├── dto/                                # Request / Response DTOs by domain
        ├── entity/                             # JPA entities
        ├── repository/                         # Spring Data JPA repositories
        ├── service/                            # Business logic layer
        └── SeatServiceApplication.java
```

---

## Domain Model

### Entities & Relationships

```
City ──< Venue ──< Seat
              └──< Event ──< EventSeatStatus ──< BookingSeat
                                                      └── Booking
```

| Entity            | Description                                                            |
| ----------------- | ---------------------------------------------------------------------- |
| `City`            | Geographic location for venue grouping                                 |
| `Venue`           | Physical location with a type (STADIUM, ARENA, etc.)                  |
| `Seat`            | A specific physical seat in a venue with type and row/number           |
| `Event`           | An event at a venue with status, category, booking window, and pricing |
| `EventSeatStatus` | Per-event seat availability snapshot (AVAILABLE / LOCKED / BOOKED)    |
| `Booking`         | A confirmed reservation by a user for one or more seats               |
| `BookingSeat`     | Join table linking a booking to specific `EventSeatStatus` records     |

### Enums

| Enum               | Values                                      |
| ------------------ | ------------------------------------------- |
| `EventStatus`      | `DRAFT`, `ACTIVE`, `SOLD_OUT`, `COMPLETED`, `CANCELLED` |
| `EventCategory`    | (e.g., `MUSIC`, `SPORTS`, `THEATRE`, ...)   |
| `SeatType`         | (e.g., `STANDARD`, `VIP`, `PREMIUM`, ...)  |
| `SeatBookingStatus`| `AVAILABLE`, `LOCKED`, `BOOKED`             |
| `BookingStatus`    | `CONFIRMED`, `CANCELLED`                    |
| `VenueType`        | (e.g., `ARENA`, `STADIUM`, `THEATRE`, ...)  |

---

## Security Model (Auth0 JWT)

All reads on public domains are **open** (no token required).  
Writes and user-specific reads require a valid **Auth0 JWT** with the appropriate permission scope.

| Permission        | Grants Access To                              |
| ----------------- | --------------------------------------------- |
| `admin:cities`    | Create / update / delete cities               |
| `admin:venues`    | Create / update / delete venues               |
| `admin:seats`     | Create / update / delete seats                |
| `admin:events`    | Create / update / delete events               |
| `admin:all`       | Admin-only event views + admin stats          |
| `write:locks`     | Lock seats before booking                     |
| `delete:locks`    | Unlock seats                                  |
| `write:bookings`  | Confirm a booking                             |
| `read:bookings`   | View booking details / user bookings          |
| `delete:bookings` | Cancel a booking                              |

---

## API Reference

### Events — `/api/events`

| Method   | Endpoint                          | Auth           | Description                                   |
| -------- | --------------------------------- | -------------- | --------------------------------------------- |
| `GET`    | `/api/events/{id}`                | Public         | Get event detail by ID                        |
| `GET`    | `/api/events/{eventId}/seats`     | Public         | All seats for an event with booking status    |
| `GET`    | `/api/events/{eventId}/seats/available` | Public   | Only available seats for an event             |
| `GET`    | `/api/events/bookable`            | Public         | Events currently within their booking window  |
| `GET`    | `/api/events/search?query=&city=` | Public         | Full-text search with optional city filter    |
| `GET`    | `/api/events/status?status=&category=` | Public   | Filter by `ACTIVE` or `SOLD_OUT`, with optional category |
| `GET`    | `/api/events/range?from=&to=`     | Public         | Events within a start-time range              |
| `GET`    | `/api/events/venue/{venueId}`     | Public         | All events at a specific venue                |
| `POST`   | `/api/events`                     | `admin:events` | Create a new event                            |
| `PUT`    | `/api/events/{id}`                | `admin:events` | Update an event                               |
| `DELETE` | `/api/events/{id}`                | `admin:events` | Delete an event                               |
| `GET`    | `/api/events/admin/all`           | `admin:all`    | All events (any status) — admin view          |
| `GET`    | `/api/events/admin/status?status=`| `admin:all`    | Admin filter by any `EventStatus`             |
| `GET`    | `/api/events/admin/venue/{venueId}/range` | `admin:all` | Admin: events at a venue within a date range |

---

### Bookings — `/api/bookings`

| Method   | Endpoint               | Auth               | Description                                |
| -------- | ---------------------- | ------------------ | ------------------------------------------ |
| `POST`   | `/api/bookings/lock`   | `write:locks`      | Lock seats for the authenticated user      |
| `DELETE` | `/api/bookings/lock`   | `delete:locks`     | Unlock seats (body: list of seat IDs)      |
| `POST`   | `/api/bookings`        | `write:bookings`   | Confirm a booking from locked seats        |
| `DELETE` | `/api/bookings/{id}`   | `delete:bookings`  | Cancel a booking                           |
| `GET`    | `/api/bookings/{id}`   | `read:bookings`    | Get booking detail by ID                   |
| `GET`    | `/api/bookings/user`   | `read:bookings`    | Get all bookings for the authenticated user|

**Booking flow:** `Lock seats → Confirm booking → Cancel (if needed)`

---

### Seats — `/api/seats`

| Method   | Endpoint                     | Auth          | Description                       |
| -------- | ---------------------------- | ------------- | --------------------------------- |
| `GET`    | `/api/seats/venue/{venueId}` | Public        | All seats at a venue              |
| `GET`    | `/api/seats/{seatId}`        | Public        | Get a specific seat               |
| `POST`   | `/api/seats`                 | `admin:seats` | Create a new seat                 |
| `PUT`    | `/api/seats/{seatId}`        | `admin:seats` | Update a seat                     |
| `DELETE` | `/api/seats/{seatId}`        | `admin:seats` | Delete a seat                     |

---

### Venues — `/api/venues`

| Method   | Endpoint            | Auth           | Description                                       |
| -------- | ------------------- | -------------- | ------------------------------------------------- |
| `GET`    | `/api/venues`       | Public         | List venues (filter by `cityId`, `isActive`)      |
| `GET`    | `/api/venues/{id}`  | Public         | Get venue by ID                                   |
| `POST`   | `/api/venues`       | `admin:venues` | Create a venue                                    |
| `PUT`    | `/api/venues/{id}`  | `admin:venues` | Update a venue                                    |
| `DELETE` | `/api/venues/{id}`  | `admin:venues` | Delete a venue                                    |

---

### Cities — `/api/cities`

| Method   | Endpoint                     | Auth           | Description                       |
| -------- | ---------------------------- | -------------- | --------------------------------- |
| `GET`    | `/api/cities`                | Public         | All cities                        |
| `GET`    | `/api/cities/{id}`           | Public         | Get city by ID                    |
| `GET`    | `/api/cities/filter?state=`  | Public         | Filter cities by state            |
| `POST`   | `/api/cities`                | `admin:cities` | Create a city                     |
| `PUT`    | `/api/cities/{id}`           | `admin:cities` | Update a city                     |
| `DELETE` | `/api/cities/{id}`           | `admin:cities` | Delete a city                     |

---

### Admin — `/api/admin`

| Method | Endpoint           | Auth        | Description                                             |
| ------ | ------------------ | ----------- | ------------------------------------------------------- |
| `GET`  | `/api/admin/stats` | `admin:all` | Platform-wide aggregate stats (bookings, events, etc.)  |

---

## Environment Configuration

Create a `.env` file inside `seat_service/` (or at root for Docker Compose) based on the following variables:

```env
# PostgreSQL
POSTGRES_IMAGE=postgres:16-alpine
POSTGRES_CONTAINER_NAME=seatforge-db
POSTGRES_USER=your_user
POSTGRES_PASSWORD=your_password
POSTGRES_DB=seatforge
PGDATA=/var/lib/postgresql/data/pgdata
POSTGRES_PORT=5432
POSTGRES_VOLUME=postgres_data
POSTGRES_HEALTH_USER=your_user
POSTGRES_HEALTH_INTERVAL=10s
POSTGRES_HEALTH_TIMEOUT=5s
POSTGRES_HEALTH_RETRIES=5

# Redis
REDIS_PORT=6379
REDIS_VOLUME=redis_data

# Auth0
AUTH0_AUDIENCE=your_auth0_api_audience
AUTH0_ISSUER_URI=https://your-tenant.auth0.com/
```

In `application.properties` / `application.yml`:

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=${AUTH0_ISSUER_URI}
auth0.audience=${AUTH0_AUDIENCE}
```

---

## Running Locally

### Prerequisites

* Docker & Docker Compose
* Java 21 (for building outside Docker)

### 1. Build the JAR

```bash
cd seat_service
./gradlew bootJar
```

### 2. Build the Docker image

```bash
docker build -t seat-service:latest .
```

### 3. Start the full stack

```bash
# From the project root
docker compose up -d
```

This starts:
* `N` replicas of `seat-service` (default: 1)
* `nginx` on port `80` as the load balancer
* `postgres` on the configured port
* `redis` on the configured port

### 4. Scale `seat-service`

```bash
docker compose up -d --scale seat-service=10
```

No config changes required — NGINX resolves Docker DNS automatically.

---

## Load Testing

```bash
autocannon -c 1000 -p 2 -d 30 http://localhost/api/events/bookable
```

### Observed Results (Local)

| Replicas      | Throughput        | Avg Latency    | Peak Latency       |
| ------------- | ----------------- | -------------- | ------------------ |
| 3 containers  | ~3,000 RPS        | ~100 ms        | ~400 ms            |
| 10 containers | ~9,000–10,000 RPS | ~200 ms        | ~800 ms (saturation)|

> Latency increase at high RPS is expected due to queueing (Little's Law). The system remains stable — no crash loops, no dropped requests.

---

## Core Design Principles

### Stateless Service

* No session state stored in the JVM
* Each request can be handled by any replica
* JWT carries all auth context — no server-side sessions

### Redis for Distributed Locking

* Seat locks are stored in Redis with a TTL
* Prevents double-booking across replicas
* Redisson client used for distributed lock primitives

### PostgreSQL Indexing

Key indexes on the `event` table:

| Index                     | Columns                          |
| ------------------------- | -------------------------------- |
| `idx_event_venue`         | `venue_id`                       |
| `idx_event_start`         | `start_time`                     |
| `idx_event_status`        | `status`                         |
| `idx_event_venue_start`   | `venue_id, start_time`           |
| `idx_event_booking_window`| `booking_open_at, booking_close_at` |

### Automated Event Status Transitions

* `ACTIVE` → `SOLD_OUT` when available seats reach zero
* `SOLD_OUT` → `ACTIVE` when a cancellation frees seats
* Any → `COMPLETED` once the event's `endTime` has passed

---

## Cloud Deployment (AWS)

| Component    | Service              |
| ------------ | -------------------- |
| Compute      | ECS (Fargate)        |
| Load Balancer| Application Load Balancer (ALB) |
| Database     | RDS (PostgreSQL)     |
| Cache        | ElastiCache (Redis)  |
| Registry     | ECR                  |
| Observability| CloudWatch           |

### Auto-Scaling

* Minimum tasks: 3
* Maximum tasks: 10+
* Triggered by: CPU utilization, request load

---

## Health & Observability

* `/actuator/health` — liveness / readiness probe (Spring Actuator)
* `/actuator/metrics` — JVM, HTTP, and pool metrics
* Structured logging via SLF4J / Logback
* Integrates with CloudWatch Logs in AWS

---

## Future Improvements

* Prometheus + Grafana dashboards for local observability
* OpenTelemetry distributed tracing
* WebSocket / SSE for real-time seat availability updates
* Rate limiting (per user / per IP) at the NGINX or gateway layer
* HTTP/2 support
* JVM tuning (G1GC configuration, heap sizing per replica tier)

---

## Key Takeaway

> **High throughput is achieved through architecture, not magic.**

* Stateless replicas — scale horizontally with zero config changes
* Distributed locking — correctness under concurrency
* Redis caching — reduce DB pressure on hot read paths
* Simple, well-understood components — easy to operate and reason about
