# Run of show: presenter script

> **Spoilers.** This folder and `solutions/` are excluded from search in `.vscode/settings.json`, so Copilot doesn't find the answers during the demo. Don't open these files in an editor tab while presenting, because open tabs can be used as context.

Total: **48 minutes**. Times are cumulative. Each block lists what to say, what to click or paste, and what you should see on **Node** and **Java**.

---

## Before the session (15 min, the day before is better)

1. Install dependencies with the **Setup: install dependencies** task, or use the dev container. Run `mvn -q test` once so the Maven cache is warm.
2. Reset to the start state:

   ```bash
   git checkout -- client server-node server-java specs docs/API.md
   git clean -fd client server-node server-java specs
   node scripts/restore-build.mjs
   rm -f logs/app.log
   ```

3. In VS Code, sign in to Copilot, choose your model, and close every editor tab.
4. Run **Storefront: run with Node API** (or **Java API**). Open <http://localhost:5173> and check the header badge.
5. Open `logs/app.log` in a split pane (it's created when the API starts).
6. Do a dry run of every Module 7 symptom in the UI so you know the numbers on your machine.

**Choosing a stack:** Node is quicker to restart and the tests run faster. Java has the subtler idiomatic bug (Integer caching) and Cucumber output that reads well to QA-heavy audiences. Everything below works with both.

---

## 0:00–3:00 · Kickoff

**Say:** "This is one app that's still in development. We'll add a feature, chase real production-style bugs, and then lock it down with tests. Copilot never sees a toy snippet: it works in the whole repo the entire time."

**Show (30 s each):**
- The running app: Shop, Orders, Inventory, and the backend badge
- `.github/copilot-instructions.md`, especially the **parity rule** and the log format
- `.github/prompts/` and `.github/agents/`: "We'll use these as slash commands and agents."

---

## 0:03–0:16 · Module 6: scaffolding

### 0:03 Line-level completion (2 min)
Follow [modules/06-scaffolding](../../modules/06-scaffolding/README.md) step 1. Accept the suggestion and point out that it used `NotFoundError` / `NotFoundException` without being told to.

### 0:05 Feature scaffold (9 min)
Switch Chat to **Agent** mode and paste:

```text
/scaffold-feature spec=specs/loyalty-points/FEATURE.md stack=node
```

**Expect:** a file plan of about 6–9 items:

| Node | Java |
|---|---|
| `src/services/loyaltyCalculator.js`, `loyaltyService.js`, `src/routes/loyalty.js`, edits to `app.js`, `orderService.js`, `errors.js` | `loyalty/` package (Calculator, Service, Controller, records), `InsufficientPointsException`, edits to `Order`, `OrderService` |
| `client/src/api/client.js`, `components/LoyaltyPanel.jsx`, `App.jsx` | same |
| `test/loyalty.test.js` | `LoyaltyControllerTest` |

**Say while it builds:** "It found the pricing feature and is copying its shape: constructor injection, the error hierarchy, the logger. That's the instructions file plus `#codebase` grounding."

**Verify:** select Ada (C-100) and confirm she has 120 points. Order the trail shoes ($97.19), which earns 97 points. Redeem 50.

**Fallback:** if it runs long or goes off track, stop it, run **Demo: jump to stage → 01-scaffold**, restart the API, and keep going.

### 0:14 Prompt file anatomy (2 min)
Open `scaffold-feature.prompt.md` and cover the front matter, `${input:}` variables, and plan-before-code.

---

## 0:16–0:32 · Module 7: debugging and RCA

### 0:16 Create the evidence (1 min)
In the UI: add **Gift Card**, then use **Rain Jacket + SAVE10**, then **SAVE99**, then **Inventory → Reconcile**. Or run **Demo: generate traffic**.

| Symptom | Node | Java |
|---|---|---|
| Gift Card in cart | 500 + request ID | 500 + request ID |
| Rain Jacket ×1 + SAVE10 | tax **11.92**, total **146.02** | same |
| SAVE99 | "Promo SAVE99 ✓ −$0.00" (silently accepted) | 400 `INVALID_PROMO` (correct) |
| Reconcile the default sheet | 500 | 500 |

### 0:17 Runtime exception (5 min)
```text
/rca Checkout with a Gift Card in the cart returns 500. Request ID <id>. #file:logs/app.log
```

**Answer key:**
- **Node:** `TypeError: Cannot read properties of null (reading 'amount')` in `pricingService.js#priceLine`. SKU-1006 has `"price": null` in the seed data.
- **Java:** `NullPointerException: Cannot invoke "Price.amount()" because the return value of "Product.price()" is null` in `PricingService.priceLine`.
- **Root cause:** a product that hasn't been priced yet can be added to a cart, and pricing assumes every product has a price.
- **Good fix:** validate and return 400 with a clear message. Bad fixes: price it at `0`, or wrap it in `try/catch`. The contract in `solutions/02-debug/docs/API.md` adds the rule.

**Talking point:** "Notice it asked whether the real problem is upstream. Should unpriced products even be listed? That's a product decision, not a null check."

Then switch to the **debugger** agent: *"Fix it using the option you recommended. Write the failing test first."* It should write the test, run it red, fix, and run it green.

### 0:22 Silent logic defect (4 min)
Paste the Module 7 step 2 prompt with your numbers.

**Answer key (both stacks):** tax is calculated on `subtotal` instead of `subtotal − discount`. The correct values are tax **10.73** and total **144.83**. The contract says *"Tax is charged on the **discounted** amount."* The log line `quote computed ... discount=14.9 taxRate=0.08 tax=11.92` is the evidence: 149 × 0.08 = 11.92.

**Node bonus (if time allows):** SAVE99 is accepted because `findPromo` uses `Map.get`, which returns `undefined`, but the guard checks `promo === null`. The `promo?.` optional chaining further down hides the problem instead of failing. Ask: *"Why was SAVE99 accepted? The contract says unknown codes are 400 INVALID_PROMO."* Java uses `Optional.orElseThrow()` and doesn't have this bug, which is a nice parity talking point.

### 0:26 Inventory reconciliation (3 min)
Paste the Module 7 step 3 prompt into the **debugger** agent.

| | Node | Java |
|---|---|---|
| Crash | `TypeError: Cannot read properties of undefined (reading 'onHand')` | `NullPointerException: ... because "expected" is null` (unboxing) |
| Cause | SKU-99871 isn't registered | same |
| After a "skip unknown" fix only | 2 variance rows, **avg 0.5** | **4 rows** (SKU-1002 and SKU-1003 show variance 0), **avg 0.25** |
| Remaining bug | the average uses the signed variance (−2 + 3) / 2 | signed average **and** `Integer != Integer` compares references, and values above 127 aren't cached |
| Correct | rows SKU-1001 (−2) and SKU-1004 (+3), avg **2.5**, `unknownSkus: ["SKU-99871"]` | same |

**Java talking point:** "250 equals 250, but `new Integer(250) != new Integer(250)`. The JVM only caches −128 to 127. That's a bug that unit tests with small numbers never catch." If Copilot's first fix unboxes to `int`, it fixes this by accident. Point that out and ask it to add a test with stock above 127.

### 0:29 Build error (3 min)
1. Run **Demo: break the build** and choose your stack.
2. In a **regular terminal** (not a task terminal), run:
   - Node: `cd server-node && npm start` → `SyntaxError: The requested module './services/pricingService.js' does not provide an export named 'PricingService'`
   - Java: `cd server-java && mvn -q compile` → `cannot find symbol: method quote(QuoteRequest)` in `CartController` and `OrderService`
3. Use the `#terminalLastCommand` prompt from the module README.

**Expect:** Copilot recognizes a half-finished rename and offers two options: revert the rename, or update every caller. Ask it which one keeps the public API stable.

**Skip:** run **Demo: restore the build**.

**Fallback for the whole module:** **Demo: jump to stage → 02-debug**.

---

## 0:32–0:46 · Module 8: testing (choose 2–3)

| If the audience is… | Run |
|---|---|
| Developers | A (TDD) + D (/doc) + C plan only |
| QA / BA heavy | B (BDD) + C (SDD) |
| Mixed (default) | A (2 loops) + B |

### A. TDD (5 min)
`/tdd-next requirements=specs/loyalty-points/REQUIREMENTS.md stack=<stack>`

- **Loop 1:** requirement 2, the 10% bonus at ≥ $100. The test `$150 → 165` fails because v1 returns 150.
- **Loop 2:** requirement 3, `InvalidPurchaseError` / `InvalidPurchaseException` for amounts ≤ 0.
- **Watch for:** checkout must not call the calculator with 0. The REQUIREMENTS note covers this, and the service's `pointsFor` guard already handles it.

### B. BDD (4 min)
`/bdd-steps feature=specs/discount-eligibility/discount-eligibility.feature stack=<stack>`
- Node uses `jest-cucumber` with `autoBindSteps`. Java uses `cucumber-java` with a `@Suite` that selects `../specs/discount-eligibility`. The Cucumber dependencies are already in the `pom.xml`.
- **Expect:** 8 scenarios pass (4 scenarios + 4 outline rows).
- The rule that settles the "both bulk and VIP" scenario: highest `discount_percent` wins.

### C. SDD (4–8 min)
`/sdd-plan spec=specs/password-reset/spec.md stack=<stack>`. Compare the result with `solutions/03-tests/specs/password-reset/plan.md`. A strong plan:
- hashes tokens before storing them,
- injects the clock and the random source,
- maps every FR/NFR to a test.

Questions to ask the room: "What would leak if the token store were dumped?" and "How do we test a 15-minute expiry without waiting 15 minutes?"

### D. /doc (1–2 min)
Select `LoyaltyService` and run `/doc`.

**Fallback:** **Demo: jump to stage → 03-tests**, then run the test task to show everything green.

---

## 0:46–0:48 · Wrap-up

- **Scaffold:** a spec plus repo instructions gives consistent, multi-layer code.
- **Debug:** evidence first, root cause before fix, then prove it with a test.
- **Test:** keep the loop small (TDD), make behavior the source of truth (BDD), and plan before code (SDD).
- Parity across two stacks shows that Copilot follows *your* conventions, not one favorite framework.

---

## Troubleshooting

| Problem | Fix |
|---|---|
| Badge says `offline` | The API isn't running, or it's on the wrong port. Only one backend can use 3001. |
| Port 3001 in use | `lsof -ti :3001 \| xargs kill` |
| Java changes not picked up | Stop and restart **Storefront: start Java API**. There's no devtools hot reload. |
| `#terminalLastCommand` is empty | Run the command in a normal terminal, not a task terminal |
| Copilot quotes the answer key | Check that `search.exclude` in `.vscode/settings.json` is still in place and that no solution files are open |
| The Node test command shows an ESM warning | It's expected and suppressed in `npm test`. If you call `jest.fn` and get `jest is not defined`, add `import { jest } from '@jest/globals'`. |
