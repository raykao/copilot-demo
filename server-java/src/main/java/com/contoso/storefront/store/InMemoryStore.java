package com.contoso.storefront.store;

import com.contoso.storefront.domain.Customer;
import com.contoso.storefront.domain.InventoryItem;
import com.contoso.storefront.domain.Order;
import com.contoso.storefront.domain.Product;
import com.contoso.storefront.domain.PromoCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/** In-memory data store seeded from data/seed.json. Everything resets on restart. */
public class InMemoryStore {

  private final Map<String, Product> products;
  private final Map<String, Integer> stockLevels;
  private final Map<String, PromoCode> promos;
  private final Map<String, Customer> customers;
  private final List<Order> orders = Collections.synchronizedList(new ArrayList<>());
  private final AtomicInteger nextOrderNumber = new AtomicInteger(1001);

  /**
   * Creates a store from seed data.
   *
   * @param seed parsed seed file
   */
  public InMemoryStore(SeedData seed) {
    this.products = index(seed.products(), Product::sku);
    this.promos = index(seed.promos(), PromoCode::code);
    this.customers = new ConcurrentHashMap<>(index(seed.customers(), Customer::id));
    this.stockLevels = new ConcurrentHashMap<>();
    for (InventoryItem item : seed.inventory()) {
      stockLevels.put(item.sku(), item.onHand());
    }
  }

  private static <T> Map<String, T> index(List<T> items, Function<T, String> key) {
    Map<String, T> map = new LinkedHashMap<>();
    items.forEach(item -> map.put(key.apply(item), item));
    return map;
  }

  /**
   * Lists the product catalog.
   *
   * @return all products in seed order
   */
  public List<Product> listProducts() {
    return List.copyOf(products.values());
  }

  /**
   * Finds a product by SKU.
   *
   * @param sku product SKU
   * @return the product, if it exists
   */
  public Optional<Product> findProduct(String sku) {
    return Optional.ofNullable(products.get(sku));
  }

  /**
   * Live stock levels keyed by SKU. Callers may update quantities in place.
   *
   * @return mutable map of SKU to quantity on hand
   */
  public Map<String, Integer> stockLevels() {
    return stockLevels;
  }

  /**
   * Finds a promo code by its normalized (upper-case) code.
   *
   * @param code promo code
   * @return the promo, if it exists
   */
  public Optional<PromoCode> findPromo(String code) {
    return Optional.ofNullable(promos.get(code));
  }

  /**
   * Lists all customers.
   *
   * @return customers in seed order
   */
  public List<Customer> listCustomers() {
    return customers.values().stream().sorted((a, b) -> a.id().compareTo(b.id())).toList();
  }

  /**
   * Finds a customer by ID.
   *
   * @param id customer ID
   * @return the customer, if it exists
   */
  public Optional<Customer> findCustomer(String id) {
    return Optional.ofNullable(customers.get(id));
  }

  /**
   * Replaces a customer record.
   *
   * @param customer the updated customer
   */
  public void saveCustomer(Customer customer) {
    customers.put(customer.id(), customer);
  }

  /**
   * Allocates the next order ID, e.g. {@code ORD-1001}.
   *
   * @return a new unique order ID
   */
  public String nextOrderId() {
    return "ORD-" + nextOrderNumber.getAndIncrement();
  }

  /**
   * Records a placed order.
   *
   * @param order the order to keep
   */
  public void saveOrder(Order order) {
    orders.add(order);
  }

  /**
   * Lists every order placed since startup.
   *
   * @return orders in placement order
   */
  public List<Order> listOrders() {
    synchronized (orders) {
      return List.copyOf(orders);
    }
  }
}
