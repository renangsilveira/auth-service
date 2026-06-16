# ADR-003: IP-Based Rate Limiting with Redis Sliding Window

## Status
Accepted

## Context
Authentication endpoints are high-value targets for brute-force and credential-stuffing attacks. Without rate limiting, an attacker can make unlimited login attempts.

## Decision
Implement IP-based rate limiting on all authentication endpoints (`/register`, `/login`, `/refresh`, `/logout`) using a Redis counter with a sliding window of 60 seconds and a threshold of 10 requests per window.

The counter key format is `rate_limit:{ip}` and is set with a 60-second TTL on first request. Subsequent requests increment the counter atomically. When the counter exceeds the threshold, the endpoint returns `429 Too Many Requests` with a `Retry-After` header.

Rate limiting can be disabled via `rateLimit.enabled=false` in configuration, which is used in integration tests to avoid interference between test cases.

## Consequences
- Brute-force attacks are significantly slowed without blocking legitimate users
- Redis atomic increments prevent race conditions under high concurrency
- IP-based limiting can be bypassed by rotating IPs (acceptable tradeoff for simplicity)
- The `/health` and `/me` endpoints are excluded from rate limiting
- Integration tests disable rate limiting to avoid flakiness from shared IPs in CI
