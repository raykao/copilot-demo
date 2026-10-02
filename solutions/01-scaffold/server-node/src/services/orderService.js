import { NotFoundError, OutOfStockError } from '../errors.js';

/** Places orders: prices the cart, reserves stock and records the order. */
export class OrderService {
  #store;
  #pricing;
  #loyalty;
  #log;

  constructor({ store, pricingService, loyaltyService, logger }) {
    this.#store = store;
    this.#pricing = pricingService;
    this.#loyalty = loyaltyService;
    this.#log = logger;
  }

  /**
   * @param {{ items: Array<{ sku: string, quantity: number }>, promoCode?: string, customerId?: string }} request
   */
  placeOrder({ items, promoCode, customerId } = {}) {
    if (customerId && !this.#store.findCustomer(customerId)) {
      throw new NotFoundError(`Customer ${customerId} not found`);
    }

    const quote = this.#pricing.quote({ items, promoCode });

    for (const line of quote.lineItems) {
      const stock = this.#store.findStock(line.sku);
      if (!stock || stock.onHand < line.quantity) {
        throw new OutOfStockError(`Only ${stock?.onHand ?? 0} of ${line.sku} left in stock`);
      }
    }
    for (const line of quote.lineItems) {
      this.#store.findStock(line.sku).onHand -= line.quantity;
    }

    const pointsEarned = customerId ? this.#loyalty.pointsFor(quote.total) : 0;
    const order = this.#store.saveOrder({
      customerId: customerId ?? null,
      ...quote,
      pointsEarned,
      createdAt: new Date().toISOString(),
    });
    if (pointsEarned > 0) {
      this.#loyalty.credit(customerId, pointsEarned, order.id);
    }
    this.#log.info('order placed', { orderId: order.id, customerId: order.customerId ?? 'guest', total: order.total });
    return order;
  }

  listOrders() {
    return this.#store.listOrders();
  }
}
