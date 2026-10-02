# Feature brief: Loyalty points (v1)

> **Module 6 scaffolding target.** The data is already there: every customer in `data/seed.json` has a `loyaltyPoints` balance. Nothing reads or updates it yet.

## User story

As a returning customer, I want to earn points when I place an order and see and redeem my balance at checkout, so I have a reason to come back.

## API (add to both backends and to `docs/API.md`)

### `GET /api/customers/{id}/loyalty`
`200 { "customerId": "C-100", "points": 120 }`
`404 NOT_FOUND` for an unknown customer.

### `POST /api/customers/{id}/loyalty/redeem`
Request: `{ "points": 50 }`
`200 { "customerId": "C-100", "redeemed": 50, "points": 70 }` (`points` is the remaining balance)

| Case | Response |
|---|---|
| `points` missing, not an integer, or < 1 | `400 VALIDATION_ERROR` |
| `points` greater than the balance | `409 INSUFFICIENT_POINTS` |
| unknown customer | `404 NOT_FOUND` |

### Earning points on checkout
- When `POST /api/orders` includes a `customerId`, the customer earns points on the order `total`.
- The order response gains `"pointsEarned": <int>`. It's `0` for guest checkouts.
- Put the earning rule in its own calculator so it can evolve on its own (see [REQUIREMENTS.md](REQUIREMENTS.md)):
  - Node: `server-node/src/services/loyaltyCalculator.js` exporting `calculatePoints(purchaseAmount)`
  - Java: `com.contoso.storefront.loyalty.LoyaltyCalculator#calculatePoints(BigDecimal)`
- **v1 rule:** 1 point per whole dollar (`$42.90 → 42`).

## UI

Add a **Loyalty** panel to the Shop sidebar, under the cart:
- Hidden for guest checkout. When a customer is selected, it shows their points balance.
- A number input and a **Redeem** button that calls the redeem endpoint, then shows the new balance or the API error with `ErrorBanner`.
- Refresh the balance after an order is placed, and show "You earned N points" in the success message.

## Logging

- `loyalty points earned customerId=… orderId=… points=…`
- `loyalty points redeemed customerId=… points=… remaining=…`

## Out of scope

- Converting points into a checkout discount
- Point expiry
- Tier multipliers and bonuses (Module 8 adds these test-first)
