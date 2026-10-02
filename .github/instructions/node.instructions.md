---
applyTo: "server-node/**"
---

# Node backend conventions

- ES modules only. Include the `.js` extension in relative imports.
- New feature layout: `src/services/<feature>Service.js` (logic), `src/routes/<feature>.js` (thin router factory taking `{ ...services }`), wired in `src/app.js` under `/api`.
- Throw subclasses of `AppError` (`ValidationError`, `NotFoundError`, ...). Add a new subclass to `src/errors.js` when no existing one fits.
- Get loggers with `logger.child('<ServiceName>')` and log events as a short message plus fields: `log.info('order placed', { orderId })`.
- Inject a `clock` function wherever the current time matters, so tests can pin it.
- Tests: Jest in ESM mode (`npm test`). In ESM, `jest` isn't a global, so use `import { jest } from '@jest/globals'` before calling `jest.fn()` or `jest.spyOn()`. Use `supertest` against `createTestApp()` for HTTP tests and construct services directly for unit tests. Use `jest-cucumber` for `.feature` files in `specs/`.
