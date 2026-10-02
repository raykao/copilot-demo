# Plan: Password reset token service

> This plan was generated from [spec.md](spec.md) and reviewed before any code was written. The spec is still the source of truth. If the two disagree, update this plan.

## Public API

| Node (`server-node/src/services/passwordReset/`) | Java (`com.contoso.storefront.auth`) |
|---|---|
| `generateResetToken(userId): string` | `String generateResetToken(String userId)` |
| `validateResetToken(token): string \| undefined` | `Optional<String> validateResetToken(String token)` |
| `consumeResetToken(token): string \| undefined` | `Optional<String> consumeResetToken(String token)` |

A blank `userId` is a caller bug, so `generateResetToken` throws `ValidationError` / `ValidationException`. Validation and consumption never throw for bad tokens (FR3).

## Storage (NFR3)

- Interface: `save(record)`, `findByHash(hash)`, `findActiveByUser(userId, now)`, `markUsed(hash, at)`
- Record: `{ tokenHash, userId, expiresAt, usedAt }`
- Records are keyed by the **SHA-256 hash** of the raw token. If the store leaks, attackers still can't recover usable tokens.
- First implementation: `InMemoryTokenStore`

## Determinism

- **Time:** an injected clock (`() => Date` in Node, `java.time.Clock` in Java). Tests move it forward.
- **Randomness:** 32 bytes from an injected CSPRNG (`crypto.randomBytes` in Node, `SecureRandom` in Java), encoded as base64url without padding, which gives 43 characters (NFR1).
- **TTL:** 15 minutes. A token is valid while `now < expiresAt`.

## Traceability

| Requirement | Test(s) |
|---|---|
| FR1 random URL-safe token, 15-min expiry | URL-safe & length; valid at 14 min; invalid at 15 min |
| FR2 validate returns userId | fresh token validates |
| FR3 no match / no throw for unknown, expired, used | unknown/empty/null token; expired; consumed |
| FR4 consume marks used and returns userId | consume then validate is empty |
| FR5 one active token per user | second token invalidates first; other users unaffected |
| NFR1 unpredictable | uses injected CSPRNG; 20 tokens are unique |
| NFR2 no secrets in logs | log output never contains the raw token (Node) |
| NFR3 swappable storage | service depends only on the store interface |
| AC: no double validate/consume | consume twice → second is empty |

## Non-goals

These come from the spec's out-of-scope list: sending email, rate limiting, and HTTP endpoints.
