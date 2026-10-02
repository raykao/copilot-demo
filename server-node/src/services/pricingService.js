import { InvalidPromoError, NotFoundError, ValidationError } from '../errors.js';
import { roundCurrency } from '../money.js';

const safeForLog = (value) => String(value).replace(/[^\w-]/g, '').slice(0, 32);

/** Prices a cart: line totals, promo discount, tax and grand total. */
export class PricingService {
  #store;
  #taxRate;
  #log;
  #clock;

  /**
   * @param {{ store: import('../store.js').Store, taxRate: number, logger: import('../logger.js').Logger, clock?: () => Date }} deps
   */
  constructor({ store, taxRate, logger, clock = () => new Date() }) {
    this.#store = store;
    this.#taxRate = taxRate;
    this.#log = logger;
    this.#clock = clock;
  }

  /**
   * @param {{ items: Array<{ sku: string, quantity: number }>, promoCode?: string }} cart
   */
  quote({ items, promoCode } = {}) {
    if (!Array.isArray(items) || items.length === 0) {
      throw new ValidationError('Cart must contain at least one item');
    }

    const lineItems = items.map((item) => this.#priceLine(item));
    const subtotal = roundCurrency(lineItems.reduce((sum, line) => sum + line.lineTotal, 0));
    const promo = this.#resolvePromo(promoCode, subtotal);
    const discount = this.#discountFor(promo, subtotal);
    const tax = roundCurrency(subtotal * this.#taxRate);
    const total = roundCurrency(subtotal - discount + tax);

    this.#log.info('quote computed', {
      items: lineItems.length,
      subtotal,
      promo: promoCode ? safeForLog(promoCode) : 'none',
      discount,
      taxRate: this.#taxRate,
      tax,
      total,
    });

    return {
      lineItems,
      subtotal,
      promo: promoCode ? { code: promoCode.trim().toUpperCase(), discount } : null,
      discount,
      tax,
      total,
    };
  }

  #priceLine({ sku, quantity } = {}) {
    if (!Number.isInteger(quantity) || quantity < 1) {
      throw new ValidationError(`Quantity for ${sku} must be a positive integer`);
    }
    const product = this.#store.findProduct(sku);
    if (!product) {
      throw new NotFoundError(`Product ${sku} not found`);
    }
    const unitPrice = product.price.amount;
    return { sku, name: product.name, unitPrice, quantity, lineTotal: roundCurrency(unitPrice * quantity) };
  }

  #resolvePromo(code, subtotal) {
    if (!code) return null;

    const promo = this.#store.findPromo(code.trim().toUpperCase());
    if (promo === null) {
      throw new InvalidPromoError(`Promo code ${code} is not valid`);
    }
    if (promo?.expiresAt && new Date(promo.expiresAt) < this.#clock()) {
      throw new InvalidPromoError(`Promo code ${code} has expired`);
    }
    if (subtotal < (promo?.minSubtotal ?? 0)) {
      throw new InvalidPromoError(`Promo code ${code} requires a minimum subtotal of ${promo.minSubtotal}`);
    }
    return promo;
  }

  #discountFor(promo, subtotal) {
    switch (promo?.type) {
      case 'percent':
        return roundCurrency((subtotal * promo.value) / 100);
      case 'fixed':
        return Math.min(promo.value, subtotal);
      default:
        return 0;
    }
  }
}
