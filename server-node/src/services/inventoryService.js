import { ValidationError } from '../errors.js';
import { roundCurrency } from '../money.js';

/** Compares physical stock counts against the system of record. */
export class InventoryService {
  #store;
  #log;

  constructor({ store, logger }) {
    this.#store = store;
    this.#log = logger;
  }

  list() {
    return this.#store.listInventory().map(({ sku, onHand }) => ({
      sku,
      name: this.#store.findProduct(sku)?.name ?? sku,
      onHand,
    }));
  }

  /**
   * @param {{ counts: Array<{ sku: string, counted: number }> }} request
   */
  reconcile({ counts } = {}) {
    if (!Array.isArray(counts) || counts.length === 0) {
      throw new ValidationError('counts must be a non-empty array');
    }
    for (const { sku, counted } of counts) {
      if (!Number.isInteger(counted) || counted < 0) {
        throw new ValidationError(`Counted quantity for ${sku} must be a non-negative integer`);
      }
    }

    this.#log.info('reconciliation started', { lines: counts.length });

    const variances = [];
    for (const { sku, counted } of counts) {
      const expected = this.#store.findStock(sku).onHand;
      if (expected !== counted) {
        variances.push({ sku, expected, counted, variance: counted - expected });
      }
    }

    const averageVariance =
      variances.length === 0
        ? 0
        : roundCurrency(variances.reduce((sum, v) => sum + v.variance, 0) / variances.length);

    this.#log.info('reconciliation complete', { lines: counts.length, variances: variances.length, averageVariance });
    return { variances, averageVariance };
  }
}
