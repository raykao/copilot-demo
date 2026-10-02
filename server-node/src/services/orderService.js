import { NotFoundError, OutOfStockError } from '../errors.js';

/** Places orders: prices the cart, reserves stock and records the order. */
export class OrderService {
  #store;
  #pricing;
  #log;

  constructor({ store, pricingService, logger }) {
    this.#store = store;
    this.#pricing = pricingService;
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

    const order = this.#store.saveOrder({
      customerId: customerId ?? null,
      ...quote,
      createdAt: new Date().toISOString(),
    });
    this.#log.info('order placed', { orderId: order.id, customerId: order.customerId ?? 'guest', total: order.total });
    return order;
  }

  listOrders() {
    return this.#store.listOrders();
  }
}
