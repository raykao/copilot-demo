#!/usr/bin/env node
// Sends a realistic burst of storefront traffic so logs/app.log has something to investigate.
// Usage: node scripts/traffic.mjs [scenario...]   (default: all scenarios)

const baseUrl = process.env.API_URL ?? 'http://localhost:3001';

const countSheet = [
  { sku: 'SKU-1001', counted: 40 },
  { sku: 'SKU-1002', counted: 250 },
  { sku: 'SKU-1003', counted: 130 },
  { sku: 'SKU-1004', counted: 21 },
  { sku: 'SKU-1005', counted: 64 },
  { sku: 'SKU-99871', counted: 12 },
];

const scenarios = {
  'quote-basic': ['POST', '/api/cart/quote', { items: [{ sku: 'SKU-1001', quantity: 1 }] }],
  'quote-save10': ['POST', '/api/cart/quote', { items: [{ sku: 'SKU-1004', quantity: 1 }], promoCode: 'SAVE10' }],
  'quote-welcome5': ['POST', '/api/cart/quote', { items: [{ sku: 'SKU-1003', quantity: 1 }], promoCode: 'WELCOME5' }],
  'quote-expired-promo': ['POST', '/api/cart/quote', { items: [{ sku: 'SKU-1001', quantity: 1 }], promoCode: 'SUMMER25' }],
  'quote-unknown-promo': ['POST', '/api/cart/quote', { items: [{ sku: 'SKU-1001', quantity: 1 }], promoCode: 'SAVE99' }],
  'quote-gift-card': ['POST', '/api/cart/quote', { items: [{ sku: 'SKU-1001', quantity: 1 }, { sku: 'SKU-1006', quantity: 1 }] }],
  'reconcile-count-sheet': ['POST', '/api/inventory/reconcile', { counts: countSheet }],
  'reconcile-known-skus': ['POST', '/api/inventory/reconcile', { counts: countSheet.filter((c) => c.sku !== 'SKU-99871') }],
  'order-save10': ['POST', '/api/orders', { items: [{ sku: 'SKU-1004', quantity: 1 }], promoCode: 'SAVE10', customerId: 'C-100' }],
};

const selected = process.argv.slice(2);
const names = selected.length > 0 ? selected : Object.keys(scenarios);

for (const name of names) {
  const scenario = scenarios[name];
  if (!scenario) {
    console.error(`Unknown scenario "${name}". Available: ${Object.keys(scenarios).join(', ')}`);
    process.exitCode = 1;
    continue;
  }
  const [method, path, body] = scenario;
  try {
    const res = await fetch(`${baseUrl}${path}`, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    const text = await res.text();
    console.log(`${name.padEnd(24)} ${res.status}  ${text.length > 220 ? `${text.slice(0, 220)}…` : text}`);
  } catch (err) {
    console.error(`${name.padEnd(24)} FAILED  ${err.cause?.code ?? err.message} - is the API running on ${baseUrl}?`);
    process.exitCode = 1;
    break;
  }
}
