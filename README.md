# SeatForge

**High-Throughput, Horizontally Scalable Event Booking Backend**

---

## Overview

**SeatForge** is a horizontally scalable backend service for event ticket booking, designed to handle **high request throughput (10,000+ RPS)** with predictable latency.

The system is built with **Spring Boot 4 (Java 21)**, secured via **Auth0 (OAuth2 / JWT)**, backed by **PostgreSQL** and **Redis**, containerized with **Docker**, and load-balanced using **NGINX** locally.  
It is fully stateless and built for **horizontal scaling**.

---

## Key Goals

* Handle **10k+ RPS** reliably
* Maintain **low and predictable latency**
* Support **horizontal scaling** without configuration changes
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

````

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
| Load Balancer    | NGINX                               |
| Load Testing     | Autocannon                          |

---

## Running Locally

### Prerequisites

* Docker & Docker Compose
* Java 21 (optional if building outside Docker)

### 1. Build the JAR

```bash
cd seat_service
./gradlew bootJar
````

### 2. Build the Docker image

```bash
docker build -t seat-service:latest .
```

### 3. Start the full stack

```bash
docker compose up -d
```

This starts:

* `N` replicas of `seat-service` (default: 1)
* `nginx` on port `80` as the load balancer
* `postgres` on the configured port
* `redis` on the configured port

### 4. Scale the service

```bash
docker compose up -d --scale seat-service=10
```

NGINX automatically load balances across containers using Docker networking.

---

## Load Testing

```bash
autocannon -c 1000 -p 2 -d 30 http://localhost/api/events/bookable
```

### Observed Results (Local Machine)

| Replicas      | Throughput        | Avg Latency | Peak Latency         |
| ------------- | ----------------- | ----------- | -------------------- |
| 3 containers  | ~3,000 RPS        | ~100 ms     | ~400 ms              |
| 10 containers | ~9,000–10,000 RPS | ~200 ms     | ~800 ms (saturation) |

> Increased latency at higher RPS is expected due to request queueing. The system remains stable without crashes or dropped requests.

---

## Core Design Principles

### Stateless Service

* No session state stored in the JVM
* Each request can be handled by any replica
* JWT carries all authentication context

### Redis for Distributed Locking

* Seat locks stored with TTL
* Prevents double booking across instances
* Uses Redisson for distributed locks

### PostgreSQL Indexing

Key indexes on the `event` table:

| Index                      | Columns                             |
| -------------------------- | ----------------------------------- |
| `idx_event_venue`          | `venue_id`                          |
| `idx_event_start`          | `start_time`                        |
| `idx_event_status`         | `status`                            |
| `idx_event_venue_start`    | `venue_id, start_time`              |
| `idx_event_booking_window` | `booking_open_at, booking_close_at` |

---

## Health & Observability

* `/actuator/health` — service health check
* `/actuator/metrics` — JVM and HTTP metrics
* Logging via SLF4J / Logback

---

## Future Improvements

* Prometheus + Grafana for monitoring
* Distributed tracing (OpenTelemetry)
* Real-time seat updates (WebSocket / SSE)
* Rate limiting at NGINX level
* JVM tuning for high-load scenarios

---

## Key Takeaway

> **High throughput comes from good architecture, not just hardware.**

* Stateless services enable easy scaling
* Redis ensures consistency under concurrency
* Caching reduces database pressure
* Simple components keep the system predictable and stable

