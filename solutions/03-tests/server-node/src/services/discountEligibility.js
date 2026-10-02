/**
 * @typedef {{ tier: string, minimumItems: number, minimumSubtotal: number, discountPercent: number }} DiscountTier
 */

/**
 * Picks the most generous discount tier whose thresholds the cart meets (thresholds are inclusive).
 * Driven by specs/discount-eligibility/discount-eligibility.feature.
 * @param {{ itemCount: number, subtotal: number }} cart
 * @param {DiscountTier[]} tiers
 * @returns {DiscountTier}
 */
export const determineDiscountTier = ({ itemCount, subtotal }, tiers) =>
  tiers
    .filter((t) => itemCount >= t.minimumItems && subtotal >= t.minimumSubtotal)
    .reduce((best, t) => (t.discountPercent > best.discountPercent ? t : best), {
      tier: 'none',
      minimumItems: 0,
      minimumSubtotal: 0,
      discountPercent: 0,
    });
