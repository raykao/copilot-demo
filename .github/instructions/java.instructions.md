---
applyTo: "server-java/**"
---

# Java backend conventions

- New feature layout: package `com.contoso.storefront.<feature>` containing a `@Service`, a `@RestController` mapped under `/api`, and request/response records.
- Throw subclasses of `StorefrontException` from `com.contoso.storefront.error`. Add a new subclass when no existing one fits.
- Inject `java.time.Clock` wherever the current time matters, so tests can pin it.
- Customer records are immutable. To change a customer, build a new record and save it with `InMemoryStore.saveCustomer`.
- Tests:
  - Unit tests construct services directly with `TestStores.seeded()`.
  - HTTP tests use `@SpringBootTest` + `@AutoConfigureMockMvc`.
  - Cucumber step definitions go under `src/test/java`, and feature files are read from `../specs`.
