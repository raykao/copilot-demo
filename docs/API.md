# Storefront API contract

Both backends (`server-node` and `server-java`) implement this contract on port **3001**, so the React client works with either one. If you change one backend, make the same change to the other.

All request and response bodies are JSON. Money values are numbers in USD rounded to 2 decimal places.

## Errors

Every error response has the same shape:

```json
{ "error": { "code": "INVALID_PROMO", "message": "Promo code SUMMER25 has expired" } }
```

| Status | `code` | When |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Malformed body, missing items, non-positive quantity |
| 400 | `INVALID_PROMO` | Unknown, expired, or below-minimum promo code |
| 404 | `NOT_FOUND` | Unknown product, customer, or route |
| 409 | `OUT_OF_STOCK` | Not enough stock to place an order |
| 500 | `INTERNAL_ERROR` | Anything unexpected. Includes `requestId` so you can find the stack trace in `logs/app.log` |

Stack traces never appear in responses. Every response carries an `x-request-id` header, and every log line includes the same ID.

## Endpoints

### `GET /api/health`
`{ "status": "ok", "backend": "node" | "java" }`

### `GET /api/products`
`[{ "sku", "name", "category", "price": { "amount", "currency" } | null }]`

### `GET /api/customers`
`[{ "id", "name" }]`

### `POST /api/cart/quote`
Request:
```json
{ "items": [{ "sku": "SKU-1004", "quantity": 1 }], "promoCode": "SAVE10", "customerId": "C-100" }
```
Response:
```json
{
  "lineItems": [{ "sku", "name", "unitPrice", "quantity", "lineTotal" }],
  "subtotal": 149.00,
  "promo": { "code": "SAVE10", "discount": 14.90 },
  "discount": 14.90,
  "tax": 10.73,
  "total": 144.83
}
```

Pricing rules:

1. `lineTotal = unitPrice × quantity`
2. `subtotal = Σ lineTotal`
3. A promo code is optional. Unknown or expired codes, or a subtotal below `minSubtotal`, are rejected with `400 INVALID_PROMO`.
   - `percent` promos: `discount = subtotal × value / 100`
   - `fixed` promos: `discount = min(value, subtotal)`
4. Tax is charged on the **discounted** amount: `tax = (subtotal − discount) × taxRate`, where `taxRate` = 0.08
5. `total = subtotal − discount + tax`

### `POST /api/orders`
Same request as a quote. Prices the cart, reserves stock (or returns `409 OUT_OF_STOCK`), and returns `201` with the quote fields plus `id`, `customerId` and `createdAt`.

### `GET /api/orders`
Lists every order placed since the server started.

### `GET /api/inventory`
`[{ "sku", "name", "onHand" }]`

### `POST /api/inventory/reconcile`
Compares a physical stock count against the system of record.

Request:
```json
{ "counts": [{ "sku": "SKU-1001", "counted": 40 }] }
```
Response:
```json
{
  "variances": [{ "sku": "SKU-1001", "expected": 42, "counted": 40, "variance": -2 }],
  "averageVariance": 2.0,
  "unknownSkus": []
}
```

- `variances` includes only SKUs where `counted ≠ expected`
- `averageVariance` is the **mean absolute variance** across the SKUs in `variances` (0 when there are none)
- A counted SKU that isn't in the system of record must never fail the whole batch. It's logged as a warning, listed in `unknownSkus`, and left out of `variances`.

## Planned (not built yet)

### Loyalty points (Module 6)
See [specs/loyalty-points/FEATURE.md](../specs/loyalty-points/FEATURE.md).
