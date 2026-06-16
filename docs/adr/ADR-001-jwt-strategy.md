# ADR-001: JWT Access Token + Refresh Token Rotation

## Status
Accepted

## Context
The service needs a stateless authentication mechanism that balances security and user experience. Pure stateless JWTs are simple but offer no way to revoke sessions before expiry. Pure stateful sessions require database lookups on every request.

## Decision
Use short-lived JWT access tokens (15 minutes) paired with long-lived refresh tokens (7 days) stored in PostgreSQL. On every refresh, the old token is revoked and a new pair is issued (rotation).

Access tokens are signed with HMAC-SHA256 using a secret loaded from environment configuration. Each token includes a unique `jti` claim (UUID) to prevent duplicate key violations when tokens are generated within the same second.

## Consequences
- Access tokens expire quickly, limiting the window of exposure if leaked
- Refresh token rotation detects replay attacks: reusing a rotated token returns 401
- Stateless validation of access tokens requires no database lookup on every request
- Logout requires explicit token blacklisting (see ADR-002)
- The `jti` claim adds entropy and prevents race conditions in high-frequency token generation
