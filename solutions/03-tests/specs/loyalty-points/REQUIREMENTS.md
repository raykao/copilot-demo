# Requirements: Loyalty points calculator (v2, TDD)

> **Module 8 TDD target.** Module 6 shipped `calculatePoints` with only the v1 rule (requirement 1). Use Copilot to drive the rest with a **Red → Green → Refactor** loop: one requirement at a time, failing test first, then the minimum code to pass it.
>
> - Node: `server-node/src/services/loyaltyCalculator.js` → tests in `server-node/test/loyaltyCalculator.test.js`
> - Java: `com.contoso.storefront.loyalty.LoyaltyCalculator` → tests in `LoyaltyCalculatorTest`
>
> If you skipped Module 6, run the **Demo: jump to stage** task and choose `01-scaffold`.

## Requirements

1. `calculatePoints(purchaseAmount)` returns 1 point per whole dollar spent
   (for example, $42.90 → 42 points).
2. Purchases of $100 or more earn a 10% point bonus, rounded down
   (for example, $150 → 150 base points + 15 bonus = 165 points).
3. A negative or zero purchase amount throws an `InvalidPurchaseError` (Node) or an `InvalidPurchaseException` (Java).
4. `calculatePoints` never returns a negative number of points.
5. Points always round down to the nearest whole point. No fractional points are awarded.

## Note for checkout

Once requirement 3 is in place, checkout must not call the calculator for orders whose total is `0`. Those orders earn `0` points.
