# Copilot instructions: Contoso Outfitters storefront

## What this repo is

A small storefront that's still in development. It has one React client and **two interchangeable backends** that implement the same REST contract:

| Folder | Stack | Run | Test |
|---|---|---|---|
| `client/` | React 18 + Vite (JavaScript) | `npm run dev` (port 5173, proxies `/api` to 3001) | – |
| `server-node/` | Node 20, Express 5, ES modules | `npm run dev` (watch mode, port 3001) | `npm test` (Jest, jest-cucumber, supertest) |
| `server-java/` | Java 17, Spring Boot 3, Maven | `mvn spring-boot:run` (port 3001) | `mvn test` (JUnit 5, Mockito, AssertJ, Cucumber) |

- The API contract is in `docs/API.md`. It's the source of truth for request and response shapes, pricing rules, and error codes.
- Both backends load the same seed data from `data/seed.json` into an in-memory store, which resets on restart.
- Both backends write the same log format to stdout and to `logs/app.log`:
  `<timestamp> <LEVEL> [<requestId>] <logger> - <message> key=value ...`
  A 500 response includes a `requestId`. Search `logs/app.log` for that ID to find the stack trace.
- Feature specs live in `specs/` (loyalty points, discount eligibility, password reset).

## Rules for every change

- **Backends may evolve independently.** Implement changes in the selected backend only unless the task asks for both. Update `docs/API.md` to describe the behavior and identify which backend or backends support it.
- Fix root causes, not symptoms. Don't hide errors with `try/catch`, default values, or `?? 0` unless the contract says so.
- Validate input at the boundary (routes and controllers, or the top of the service method). Return the contract's error envelope `{ "error": { "code", "message" } }` with the right status.
- Never put stack traces or internal details in API responses.
- Never log PII (customer email or name), credentials, or tokens. Customer IDs are fine.
- Use the existing logger with structured `key=value` fields. Don't add `console.log` or `System.out`.

## Node (`server-node/`)

- For local development and API verification, start the server with `npm run dev` or the **Storefront: start Node API** task. Do not use `npm start` for active development; it does not watch source files. Before starting another server, check whether an existing process owns port 3001 instead of assuming it has loaded current code.
- ES modules (`import`/`export`), `const` over `let`, never `var`, and `async/await` instead of `.then()`.
- Services are classes that get their dependencies through the constructor (`{ store, logger, ... }`). Routes stay thin.
- Errors extend `AppError` in `src/errors.js`.
- Money is rounded with `roundCurrency()` from `src/money.js`.
- Tests go in `test/*.test.js`, follow Arrange / Act / Assert, use `jest.fn()` and `jest.spyOn()`, and have descriptive names like `should throw InvalidPromoError when the promo code has expired`. Build apps with `createTestApp()` from `test/helpers.js`.

## Java (`server-java/`)

- Package by feature under `com.contoso.storefront` (`pricing`, `orders`, `inventory`, ...).
- Use Java 17 features (records for DTOs, switch expressions, pattern matching).
- Constructor injection only, never field `@Autowired`. Return `Optional` rather than `null`.
- Use `BigDecimal` for all money, and compare it with `compareTo`.
- Log with SLF4J parameterized messages: `log.info("order placed orderId={}", id)`.
- Errors extend `StorefrontException`.
- Every public method has Javadoc.
- Tests use JUnit 5, AssertJ and Mockito, are named `givenX_whenY_thenZ`, and are grouped with `@Nested` by method under test. Build stores with `TestStores.seeded()`.

## Client (`client/`)

- Function components and hooks. All HTTP calls go through `src/api/client.js`.
- Show API errors with `ErrorBanner`, which also shows the request ID.
