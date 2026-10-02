import { InvalidPurchaseError } from '../errors.js';

const BONUS_THRESHOLD = 100;
const BONUS_RATE = 0.1;

/**
 * Loyalty points earned for a purchase (specs/loyalty-points/REQUIREMENTS.md):
 * 1 point per whole dollar, plus a 10% bonus (rounded down) for purchases of $100 or more.
 * @param {number} purchaseAmount order total in USD
 * @returns {number} whole, non-negative points
 * @throws {InvalidPurchaseError} when the amount is zero or negative
 */
export const calculatePoints = (purchaseAmount) => {
  if (!(purchaseAmount > 0)) {
    throw new InvalidPurchaseError(`Purchase amount must be positive, got ${purchaseAmount}`);
  }
  const basePoints = Math.floor(purchaseAmount);
  const bonusPoints = purchaseAmount >= BONUS_THRESHOLD ? Math.floor(basePoints * BONUS_RATE) : 0;
  return basePoints + bonusPoints;
};
