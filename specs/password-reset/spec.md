# Spec: Password reset token service

> **Module 8 SDD target.** This is the **spec**, written before any code. Ask Copilot for an implementation **plan** from this spec (saved as `plan.md` next to this file), review it, and only then generate the implementation and tests from the plan. That way the spec stays the single source of truth.
>
> - Node: `server-node/src/services/passwordReset/`
> - Java: `com.contoso.storefront.auth`

## Purpose

Generate and validate short-lived, single-use password reset tokens for storefront customer accounts.

## Functional requirements

- **FR1:** `generateResetToken(userId)` returns a random, URL-safe token string and stores it with an expiry 15 minutes after creation.
- **FR2:** `validateResetToken(token)` returns the associated `userId` if the token exists, hasn't expired, and hasn't been used.
- **FR3:** `validateResetToken(token)` returns nothing (no match) for an unknown, expired, or already-used token. It must not throw in these cases.
- **FR4:** `consumeResetToken(token)` marks a valid token as used so it can't be validated again, and returns the associated `userId`.
- **FR5:** Each user can have only one active (unexpired, unused) token at a time. Generating a new token invalidates any earlier active token for that user.

## Non-functional requirements

- **NFR1:** Tokens must not be predictable or guessable. Use a cryptographically secure random source and a sufficient length.
- **NFR2:** Never write sensitive data (raw tokens, user PII) to logs.
- **NFR3:** Token storage must be swappable. Start with an in-memory store behind an interface, so a persistent store can be dropped in later.

## Out of scope

- Sending the reset email or notification
- Rate limiting reset requests per user or IP
- HTTP endpoints (this is a service-layer component)

## Acceptance criteria

- Every functional requirement above has at least one passing test.
- A token can't be validated or consumed twice.
- A token can't be validated after it expires.
- Generating a second token for the same user invalidates the first.
