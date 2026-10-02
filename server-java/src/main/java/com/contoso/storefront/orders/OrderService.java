package com.contoso.storefront.orders;

import com.contoso.storefront.domain.LineItem;
import com.contoso.storefront.domain.Order;
import com.contoso.storefront.error.NotFoundException;
import com.contoso.storefront.error.OutOfStockException;
import com.contoso.storefront.pricing.PricingService;
import com.contoso.storefront.pricing.Quote;
import com.contoso.storefront.pricing.QuoteRequest;
import com.contoso.storefront.store.InMemoryStore;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Places orders: prices the cart, reserves stock and records the order. */
@Service
public class OrderService {

  private static final Logger log = LoggerFactory.getLogger(OrderService.class);

  private final InMemoryStore store;
  private final PricingService pricingService;
  private final Clock clock;

  /**
   * Creates the service.
   *
   * @param store data store
   * @param pricingService pricing logic
   * @param clock clock for order timestamps
   */
  public OrderService(InMemoryStore store, PricingService pricingService, Clock clock) {
    this.store = store;
    this.pricingService = pricingService;
    this.clock = clock;
  }

  /**
   * Prices the cart, reserves stock and records the order.
   *
   * @param request cart lines, optional promo code and customer ID
   * @return the placed order
   * @throws NotFoundException if the customer does not exist
   * @throws OutOfStockException if any line cannot be fulfilled
   */
  public synchronized Order placeOrder(QuoteRequest request) {
    String customerId = request == null ? null : request.customerId();
    if (customerId != null && store.findCustomer(customerId).isEmpty()) {
      throw new NotFoundException("Customer " + customerId + " not found");
    }

    Quote quote = pricingService.quote(request);

    Map<String, Integer> stock = store.stockLevels();
    for (LineItem line : quote.lineItems()) {
      int onHand = stock.getOrDefault(line.sku(), 0);
      if (onHand < line.quantity()) {
        throw new OutOfStockException("Only " + onHand + " of " + line.sku() + " left in stock");
      }
    }
    for (LineItem line : quote.lineItems()) {
      stock.merge(line.sku(), -line.quantity(), Integer::sum);
    }

    Order order =
        new Order(
            store.nextOrderId(),
            customerId,
            quote.lineItems(),
            quote.subtotal(),
            quote.promo(),
            quote.discount(),
            quote.tax(),
            quote.total(),
            clock.instant());
    store.saveOrder(order);
    log.info(
        "order placed orderId={} customerId={} total={}",
        order.id(),
        customerId == null ? "guest" : customerId,
        order.total());
    return order;
  }

  /**
   * Lists orders placed since startup.
   *
   * @return all orders
   */
  public List<Order> listOrders() {
    return store.listOrders();
  }
}
