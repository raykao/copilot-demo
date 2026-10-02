# Module 7: Debugging and defect root cause analysis

**Time:** about 16 minutes · **Goal:** use real evidence (stack traces, logs, terminal output and the API contract) to find root causes, then prove each fix with a test.

There are several defects in the storefront, and each needs a different prompting strategy:

| Defect type | Signal | Strategy |
|---|---|---|
| Runtime exception | 500 response, stack trace in `logs/app.log` | Give Copilot the request ID and logs, and ask it to trace the call chain before fixing |
| Silent logic defect | Wrong numbers, no error | Give Copilot the expected and actual values and the contract, and ask what state produces the wrong number |
| Build error | App won't start or compile | `#terminalLastCommand`: explain *why* it's invalid, not just how to silence it |

## 0. Create evidence (1 min)

With the app running, use the UI (or run the **Demo: generate traffic** task):

1. **Shop:** add **Gift Card** to the cart.
2. **Shop:** remove it, add **Ultralight Rain Jacket**, and apply promo `SAVE10`. Write down the tax and total.
3. **Shop:** apply promo `SAVE99`.
4. **Inventory:** select **Reconcile** with the default count sheet.

Watch the API terminal and `logs/app.log` while you go.

## 1. Runtime exception from logs (5 min)

Copy the request ID from the red error banner, then run in Chat (**Ask** mode):

```text
/rca Checkout with a Gift Card in the cart returns 500. Request ID <id>. #file:logs/app.log
```

The prompt asks Copilot to classify the defect, trace it, find the *upstream* cause, list the blast radius (including the other backend), and offer fix options. It doesn't apply a fix yet.

Then switch to the **debugger** agent and ask it to fix and prove it:

```text
Fix the Gift Card checkout crash using the option you recommended. Write the failing test first.
```

## 2. Silent logic defect (4 min)

The rain jacket quote doesn't crash, but is it right?

```text
#file:docs/API.md #file:logs/app.log
For SKU-1004 x1 with promo SAVE10, the quote shows subtotal 149.00, discount 14.90,
tax <your number> and total <your number>. Using the pricing rules in the API contract,
what should tax and total be? Find the line of code responsible. Explain before you fix.
```

Then use `/fix-and-prove`.

## 3. Data integrity: inventory reconciliation (3 min)

Reconcile fails with a 500. Ask the **debugger** agent:

```text
Reconciling the warehouse count sheet in the Inventory tab returns 500. Find the request
in logs/app.log, explain the root cause, and fix it so it meets the contract in docs/API.md
(unknown SKUs are skipped and reported, and averageVariance is the mean absolute variance).
```

Reconcile again in the UI. Do the numbers match the contract? Are there rows that shouldn't be there? If so, keep going: this is where the two backends behave differently.

## 4. Build error from the terminal (3 min)

1. Run the **Demo: break the build** task (or `/break-build`) and choose your stack. A teammate has pushed a half-finished rename.
2. In a **regular terminal**, run the command it printed (`npm start` or `mvn -q compile`).
3. In Chat:

   ```text
   #terminalLastCommand Explain this failure. What was the refactor trying to do, and what's
   the minimal change that completes it without changing the public contract?
   ```

4. Apply the fix and rerun. **Demo: restore the build** undoes the breakage if you'd rather skip ahead.

## Key takeaways

- **Evidence beats description.** Request IDs, log lines and the contract turn guesses into a diagnosis.
- **Ask why before fix.** Separate root cause from patch to avoid `?.`-style band-aids.
- **Prove it.** Failing test → fix → passing test, run by Agent mode.
- **Check parity.** Two implementations of one contract fail in different ways.

## If something goes wrong

Run the **Demo: jump to stage** task and choose `02-debug`, then restart the API.

**Next:** [Module 8: Testing](../08-testing/README.md)
