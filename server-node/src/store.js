import fs from 'node:fs';

/**
 * Reads the shared seed file used by both backends.
 * @param {string} seedPath
 */
export const loadSeed = (seedPath) => JSON.parse(fs.readFileSync(seedPath, 'utf8'));

/** In-memory data store seeded from data/seed.json. Everything resets on restart. */
export class Store {
  #products;
  #stock;
  #promos;
  #customers;
  #orders = [];
  #nextOrderNumber = 1001;

  constructor(seed) {
    this.#products = new Map(seed.products.map((p) => [p.sku, p]));
    this.#stock = new Map(seed.inventory.map((i) => [i.sku, { ...i }]));
    this.#promos = new Map(seed.promos.map((p) => [p.code, p]));
    this.#customers = new Map(seed.customers.map((c) => [c.id, { ...c }]));
  }

  listProducts() {
    return [...this.#products.values()];
  }

  findProduct(sku) {
    return this.#products.get(sku);
  }

  listInventory() {
    return [...this.#stock.values()];
  }

  findStock(sku) {
    return this.#stock.get(sku);
  }

  findPromo(code) {
    return this.#promos.get(code);
  }

  listCustomers() {
    return [...this.#customers.values()];
  }

  findCustomer(id) {
    return this.#customers.get(id);
  }

  saveOrder(order) {
    const saved = { id: `ORD-${this.#nextOrderNumber++}`, ...order };
    this.#orders.push(saved);
    return saved;
  }

  listOrders() {
    return [...this.#orders];
  }
}
