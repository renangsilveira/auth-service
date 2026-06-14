# auth-service

[![CI](https://github.com/renangsilveira/auth-service/actions/workflows/ci.yml/badge.svg)](https://github.com/renangsilveira/auth-service/actions/workflows/ci.yml)

> Kotlin · Ktor · JWT · Redis · PostgreSQL · Docker

A production-grade authentication microservice built with Kotlin and Ktor, featuring JWT access tokens, refresh token rotation, token revocation, and IP-based rate limiting.

---

## What it does

`auth-service` is a standalone authentication microservice responsible for identity and session management in a distributed system.

The service:

- registers users with securely hashed passwords (BCrypt)
- issues JWT access tokens (short-lived) and refresh tokens (long-lived)
- rotates refresh tokens on every use, invalidating the previous one
- revokes access tokens via a Redis-backed blacklist
- enforces IP-based rate limiting on all authentication endpoints
- exposes a protected endpoint to validate token integrity

---

## Tech stack

| Layer              | Technology                          |
|--------------------|-------------------------------------|
| Language           | Kotlin 2.3                          |
| Framework          | Ktor 3.5 (Netty)                    |
| Authentication     | JWT (JJWT) + Refresh Token Rotation |
| Cache / Blacklist  | Redis (Lettuce)                     |
| Database           | PostgreSQL 16 + Exposed ORM         |
| Migrations         | Flyway                              |
| API docs           | OpenAPI / Swagger UI                |
| Tests              | kotlin.test + Testcontainers        |
| CI                 | GitHub Actions                      |
| Container          | Docker + Docker Compose             |

---

## Architecture
┌─────────────────────────────────┐
│          REST Clients           │
└────────────────┬────────────────┘
│ HTTP
┌────────────────▼────────────────┐
│         Ktor Routes             │
│  /auth/register                 │
│  /auth/login                    │
│  /auth/refresh                  │
│  /auth/logout                   │
│  /auth/me  (protected)          │
└──────┬──────────────┬───────────┘
│              │
┌──────▼──────┐ ┌─────▼───────────┐
│  UserService│ │  TokenService   │
│             │ │                 │
│  BCrypt     │ │  JWT generation │
│  hashing    │ │  token rotation │
└──────┬──────┘ └─────┬───────────┘
│              │
┌──────▼──────┐ ┌─────▼───────────┐
│  PostgreSQL │ │  Redis          │
│             │ │                 │
│  users      │ │  token blacklist│
│  refresh_   │ │  rate limiting  │
│  tokens     │ │  (sliding win.) │
└─────────────┘ └─────────────────┘

---

## Running locally

### Prerequisites

- Docker Desktop
- JDK 21

### 1. Start infrastructure

```bash
docker compose up -d
```

### 2. Run the application

```bash
./gradlew run
```

Service starts on `http://localhost:8080`.

### 3. Full Docker stack

```bash
docker compose --profile app up --build
```

### 4. Run tests

```bash
./gradlew test
```

### 5. Open Swagger UI

http://localhost:8080/swagger

---

## API reference

| Method   | Path                  | Auth required | Description                        |
|----------|-----------------------|---------------|------------------------------------|
| `POST`   | `/api/v1/auth/register` | No          | Register a new user                |
| `POST`   | `/api/v1/auth/login`    | No          | Authenticate and receive token pair|
| `POST`   | `/api/v1/auth/refresh`  | No          | Rotate refresh token               |
| `POST`   | `/api/v1/auth/logout`   | Yes         | Revoke access token                |
| `GET`    | `/api/v1/auth/me`       | Yes         | Get authenticated user info        |
| `GET`    | `/health`               | No          | Health check                       |

---

## Key technical decisions

### JWT access token + refresh token rotation

Access tokens are short-lived (15 minutes) to minimize exposure. Refresh tokens are long-lived (7 days) and stored in PostgreSQL. On every refresh, the old token is invalidated and a new pair is issued — preventing replay attacks.

### Redis-backed token blacklist

On logout, the access token is added to a Redis blacklist with a TTL matching its remaining validity. This avoids the need for database lookups on every authenticated request.

### IP-based rate limiting with sliding window

Authentication endpoints enforce a sliding window rate limit per IP using Redis atomic counters. Clients that exceed the threshold receive `429 Too Many Requests` with a `Retry-After` header.

### BCrypt for password hashing

Passwords are hashed with BCrypt (cost factor 12) and never stored or logged in plain text.

### Exposed ORM with Flyway migrations

Database schema is managed exclusively through versioned Flyway migrations. Exposed DSL is used for type-safe queries without the overhead of a full ORM framework.

---

## Future improvements

- OpenTelemetry distributed tracing
- OAuth2 / social login (Google, GitHub)
- Email verification flow
- Multi-factor authentication (TOTP)
