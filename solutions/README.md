# Solutions (answer key)

Each folder is a **full snapshot** of `client/`, `server-node/` and `server-java/` (plus `docs/API.md`, and `specs/` for stage 03) as they should look at the **end** of that stage.

| Stage | Adds on top of the previous stage |
|---|---|
| `01-scaffold` | Loyalty points feature (Module 6): calculator v1, service, routes/controller, `pointsEarned` on orders, `LoyaltyPanel` UI, HTTP tests |
| `02-debug` | Fixes for every Module 7 defect, plus regression tests that fail against the original code |
| `03-tests` | Module 8: loyalty calculator v2 (TDD), discount eligibility (BDD), password reset tokens (SDD, including `plan.md`) |

To jump to a stage during a demo, run the **Demo: jump to stage** task or:

```bash
node scripts/use-stage.mjs 02-debug
```

The script replaces the source in `client/`, `server-node/` and `server-java/` but keeps `node_modules/` and `target/`. Restart the API afterward.

This folder is excluded from search in `.vscode/settings.json` so Copilot can't read the answers during a live demo.
