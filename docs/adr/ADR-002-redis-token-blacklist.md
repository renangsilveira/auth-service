# ADR-002: Redis-Backed Token Blacklist for Logout

## Status
Accepted

## Context
JWT access tokens are stateless by design — once issued, they remain valid until expiry. This means a logout operation cannot truly invalidate a token without a server-side check.

## Decision
On logout, the access token is added to a Redis blacklist with a TTL equal to its remaining validity period. Every authenticated request checks the blacklist before proceeding.

The blacklist key format is `blacklist:{token}` and the value is `"revoked"`. The TTL is calculated as `(token.expiration - now) / 1000` seconds, ensuring the key is automatically cleaned up by Redis when the token would have expired anyway.

## Consequences
- Logout is immediate and effective within the same request
- Redis lookup adds a small latency (~1ms) to every authenticated request
- Blacklist entries are self-cleaning via Redis TTL — no manual cleanup required
- Redis is a required dependency; if Redis is unavailable, logout functionality degrades
- Memory usage is bounded by the number of active sessions times the token size
