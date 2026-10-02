/**
 * Loyalty points earning rule. Kept separate so it can evolve on its own (see specs/loyalty-points/REQUIREMENTS.md).
 * v1: 1 point per whole dollar spent.
 * @param {number} purchaseAmount order total in USD
 * @returns {number} points earned
 */
export const calculatePoints = (purchaseAmount) => Math.floor(purchaseAmount);
