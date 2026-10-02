import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { autoBindSteps, loadFeature } from 'jest-cucumber';
import { determineDiscountTier } from '../src/services/discountEligibility.js';

const featurePath = path.resolve(
  path.dirname(fileURLToPath(import.meta.url)),
  '../../specs/discount-eligibility/discount-eligibility.feature',
);
const feature = loadFeature(featurePath);

const steps = ({ given, when, then }) => {
  let tiers = [];
  let cart = null;
  let result = null;

  given('the store has the following discount tiers:', (table) => {
    tiers = table.map((row) => ({
      tier: row.tier,
      minimumItems: Number(row.minimum_items),
      minimumSubtotal: Number(row.minimum_subtotal),
      discountPercent: Number(row.discount_percent),
    }));
  });

  given(/^a cart with (\d+) items and a subtotal of \$([\d.]+)$/, (items, subtotal) => {
    cart = { itemCount: Number(items), subtotal: Number(subtotal) };
  });

  when('the discount tier is calculated', () => {
    result = determineDiscountTier(cart, tiers);
  });

  then(/^the applied discount tier should be "(\w+)"$/, (expected) => {
    expect(result.tier).toBe(expected);
  });

  then(/^the discount percent should be (\d+)$/, (expected) => {
    expect(result.discountPercent).toBe(Number(expected));
  });
};

autoBindSteps([feature], [steps]);
