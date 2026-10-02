import { InsufficientPointsError, NotFoundError, ValidationError } from '../errors.js';
import { calculatePoints } from './loyaltyCalculator.js';

/** Reads, earns and redeems customer loyalty points. */
export class LoyaltyService {
  #store;
  #log;

  constructor({ store, logger }) {
    this.#store = store;
    this.#log = logger;
  }

  /**
   * @param {string} customerId
   * @returns {{ customerId: string, points: number }}
   */
  getBalance(customerId) {
    const customer = this.#requireCustomer(customerId);
    return { customerId: customer.id, points: customer.loyaltyPoints };
  }

  /**
   * Points a purchase of this amount would earn.
   * @param {number} purchaseAmount
   */
  pointsFor(purchaseAmount) {
    return purchaseAmount > 0 ? calculatePoints(purchaseAmount) : 0;
  }

  /**
   * Adds earned points to a customer's balance.
   * @param {string} customerId
   * @param {number} points
   * @param {string} orderId
   */
  credit(customerId, points, orderId) {
    const customer = this.#requireCustomer(customerId);
    customer.loyaltyPoints += points;
    this.#log.info('loyalty points earned', { customerId, orderId, points });
  }

  /**
   * @param {string} customerId
   * @param {{ points: number }} request
   * @returns {{ customerId: string, redeemed: number, points: number }}
   */
  redeem(customerId, { points } = {}) {
    if (!Number.isInteger(points) || points < 1) {
      throw new ValidationError('points must be a positive integer');
    }
    const customer = this.#requireCustomer(customerId);
    if (points > customer.loyaltyPoints) {
      throw new InsufficientPointsError(
        `Customer ${customerId} has ${customer.loyaltyPoints} points; cannot redeem ${points}`,
      );
    }
    customer.loyaltyPoints -= points;
    this.#log.info('loyalty points redeemed', { customerId, points, remaining: customer.loyaltyPoints });
    return { customerId, redeemed: points, points: customer.loyaltyPoints };
  }

  #requireCustomer(customerId) {
    const customer = this.#store.findCustomer(customerId);
    if (!customer) {
      throw new NotFoundError(`Customer ${customerId} not found`);
    }
    return customer;
  }
}
