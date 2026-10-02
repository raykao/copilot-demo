# Module 6: Code development and scaffolding

**Time:** about 13 minutes · **Goal:** go from a feature brief to working, tested code across API, service and UI, following the patterns already in the repo.

## What's already here

The storefront sells products, prices carts and places orders. Every customer in `data/seed.json` already has a `loyaltyPoints` balance, but nothing reads or updates it. The brief for that feature is in [specs/loyalty-points/FEATURE.md](../../specs/loyalty-points/FEATURE.md).

## 1. Line level: comment-driven completion (2 min)

Open the catalog routes for your backend and type a comment that describes the next endpoint. Let inline suggestions write it.

- **Node:** `server-node/src/routes/catalog.js`, inside `catalogRoutes`:

  ```js
  // GET /products/:sku returns one product, or throws NotFoundError if the SKU doesn't exist
  ```

- **Java:** `server-java/src/main/java/com/contoso/storefront/web/CatalogController.java`, under `products()`:

  ```java
  /** Returns one product by SKU, or throws NotFoundException if it doesn't exist. */
  ```

Talking point: the comment *is* the prompt. Copilot picks up the error classes and style from the surrounding file.

## 2. Feature level: scaffold with Agent mode (9 min)

1. Open Copilot Chat in **Agent** mode.
2. Run the prompt file:

   ```text
   /scaffold-feature spec=specs/loyalty-points/FEATURE.md stack=node
   ```

   (Use `stack=java` for the Java backend.)
3. Copilot reads the spec, the API contract and the closest existing feature, then **proposes a file plan and stops**. Review it out loud:
   - Does it reuse `AppError` / `StorefrontException`, the logger and constructor injection?
   - Does it plan tests and an update to `docs/API.md`?
4. Reply `Approved` and let it build. It should finish by running the test suite.
5. Restart the Java API if you're using it (the Node task restarts itself on file changes). Pick **Ada Lovelace** in the cart, and check that the **Loyalty** panel shows 120 points. Place an order, then redeem some points.

If you want to steer without the prompt file, the same result can be reached with:

```text
#codebase Implement specs/loyalty-points/FEATURE.md for server-node and the client.
Follow the patterns in the pricing and orders features. Show me a file plan first.
```

## 3. Reuse: anatomy of a prompt file (2 min)

Open [.github/prompts/scaffold-feature.prompt.md](../../.github/prompts/scaffold-feature.prompt.md) and point out:

- The `agent: agent` front matter, which runs it in Agent mode
- `${input:...}` variables, so one prompt serves any spec and either stack
- The plan → approve → build → test structure, which is what makes the output predictable

## If something goes wrong

Run the **Demo: jump to stage** task and choose `01-scaffold`, then restart the API.

**Next:** [Module 7: Debugging and RCA](../07-debugging/README.md)
