# SeatForge

**High-Throughput, Horizontally Scalable Spring Boot Backend**

---

## Overview

**SeatForge** is a horizontally scalable backend service designed to handle **high request throughput (10,000+ RPS)** with predictable latency.

The system is built using **Spring Boot (Java 21)**, containerized with **Docker**, and load-balanced using **NGINX** (locally) or **AWS Application Load Balancer** (cloud).
It is fully stateless and designed for **horizontal scaling**.

---

## Key Goals

* Handle **10k RPS** reliably
* Maintain **low and predictable latency**
* Support **horizontal scaling** without configuration changes
* Be cloud-ready (AWS ECS / ALB)
* Keep the architecture simple and observable

---

## Architecture (High Level)

### Local / Development Architecture

```
Client
  ↓
NGINX (Reverse Proxy + Load Balancer)
  ↓
Spring Boot Containers (N replicas)
```

### Cloud / Production Architecture (AWS)

```
Internet
  ↓
AWS Application Load Balancer (ALB)
  ↓
ECS Service (Spring Boot Tasks, Auto-Scaled)
```

> In AWS, **ALB replaces NGINX**.
> The application itself remains unchanged.

---

## Core Principles

### 1. Stateless Design

* No session state stored in memory
* Each request can be handled by any instance
* Enables unlimited horizontal scaling

### 2. Single Image, Multiple Replicas

* One Docker image
* Many running containers
* Scaling is achieved by increasing replicas, not images

### 3. Infrastructure-Aware Load Balancing

* Locally: NGINX + Docker DNS
* Cloud: AWS ALB + ECS
* Backends are never exposed directly

---

## Technology Stack

| Layer            | Technology        |
| ---------------- | ----------------- |
| Language         | Java 21           |
| Framework        | Spring Boot       |
| Build Tool       | Gradle            |
| Containerization | Docker            |
| Local LB         | NGINX             |
| Cloud Runtime    | AWS ECS (Fargate) |
| Cloud LB         | AWS ALB           |
| Load Testing     | Autocannon        |

---

## Project Structure

```
SeatForge/
│
├── docker-compose.yml        # System-level orchestration
│
├── nginx/
│   └── nginx.conf            # Local reverse proxy + load balancer
│
├── seat-service/
│   ├── Dockerfile            # Java 21 runtime image
│   ├── build.gradle
│   ├── settings.gradle
│   └── src/main/java/
│       └── com/.../controllers
│           └── EventController.java
│
└── README.md
```

---

## API Design

### Endpoint

```
GET /api/event
```

### Sample Response

```json
{
  "status": "success"
}
```

The endpoint is intentionally lightweight to measure **raw system throughput** without business logic overhead.

---

## Docker Image Design

### Base Image

* `eclipse-temurin:21-jre-alpine`
* Java 21 runtime only (no build tools)
* Small footprint, fast startup

### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/*SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
```

---

## Local Load Balancing (NGINX)

NGINX is used **only for local testing**.

### Key Features

* Round-robin load balancing
* Connection reuse (keepalive)
* Minimal latency overhead

### NGINX Strategy

* NGINX targets the **service name**, not individual containers
* Docker DNS resolves multiple container IPs automatically
* Scaling does not require config changes

---

## Scaling Strategy

### Local Scaling (Docker Compose)

```bash
docker compose up -d --scale seat-service=10
```

* No config changes required
* Containers are added dynamically
* NGINX automatically load balances

---

### Cloud Scaling (AWS ECS)

* Minimum tasks: 3
* Maximum tasks: 10+
* Auto scaling based on:

  * CPU utilization
  * Request load

This replaces Docker Compose entirely in production.

---

## Performance Characteristics

### Observed Results (Local Testing)

| Load Test     | Result                |
| ------------- | --------------------- |
| 3 containers  | ~3,000 RPS            |
| 10 containers | ~9,000–10,000 RPS     |
| Peak latency  | ~800 ms at saturation |
| Avg latency   | ~200 ms at 10k RPS    |

> Latency increase at high RPS is expected due to queueing (Little’s Law).

---

## Load Testing

### Example Command

```bash
autocannon -c 1000 -p 2 -d 30 http://localhost/api/event
```

### Notes

* High concurrency exposes real bottlenecks
* Latency increases under saturation, not failure
* System remains stable under pressure

---

## Error Handling & Stability

* No timeouts under heavy load
* No crash loops
* Graceful degradation under saturation
* Errors primarily occur during cold start (before health checks)

---

## Cloud Deployment (AWS)

### Recommended Setup

* **ECS (Fargate)** for container runtime
* **ALB** for load balancing
* **ECR** for image storage
* **CloudWatch** for metrics and logs

### Why ECS (not Kubernetes)?

* Lower operational overhead
* Native AWS integration
* More than sufficient for 10k+ RPS

---

## What This System Is (and Is Not)

### ✅ This System Is

* Horizontally scalable
* Cloud ready
* Performance tested
* Production-oriented

### ❌ This System Is Not

* A monolith with in-memory state
* Tied to a specific cloud provider
* Dependent on NGINX in production

---

## Future Improvements

* Add `/actuator/health` for health checks
* JVM tuning (GC, heap sizing)
* HTTP/2 or gRPC
* Distributed caching (Redis)
* Observability (Prometheus, OpenTelemetry)

---

## Key Takeaway

This project demonstrates that:

> **High throughput is achieved through architecture, not magic.**

* Stateless services
* Horizontal scaling
* Simple, well-understood components
* Measure first, optimize later

---
