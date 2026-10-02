package com.contoso.storefront.loyalty;

import com.contoso.storefront.domain.Customer;
import com.contoso.storefront.error.InsufficientPointsException;
import com.contoso.storefront.error.NotFoundException;
import com.contoso.storefront.error.ValidationException;
import com.contoso.storefront.store.InMemoryStore;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Reads, earns and redeems customer loyalty points. */
@Service
public class LoyaltyService {

  private static final Logger log = LoggerFactory.getLogger(LoyaltyService.class);

  private final InMemoryStore store;
  private final LoyaltyCalculator calculator;

  /**
   * Creates the service.
   *
   * @param store data store
   * @param calculator points earning rule
   */
  public LoyaltyService(InMemoryStore store, LoyaltyCalculator calculator) {
    this.store = store;
    this.calculator = calculator;
  }

  /**
   * Returns a customer's balance.
   *
   * @param customerId customer ID
   * @return the balance
   * @throws NotFoundException if the customer does not exist
   */
  public LoyaltyBalance getBalance(String customerId) {
    Customer customer = requireCustomer(customerId);
    return new LoyaltyBalance(customer.id(), customer.loyaltyPoints());
  }

  /**
   * Points a purchase of this amount would earn.
   *
   * @param purchaseAmount order total
   * @return points, or 0 for non-positive amounts
   */
  public int pointsFor(BigDecimal purchaseAmount) {
    return purchaseAmount.signum() > 0 ? calculator.calculatePoints(purchaseAmount) : 0;
  }

  /**
   * Adds earned points to a customer's balance.
   *
   * @param customerId customer ID
   * @param points points to add
   * @param orderId order that earned them
   */
  public synchronized void credit(String customerId, int points, String orderId) {
    Customer customer = requireCustomer(customerId);
    store.saveCustomer(withPoints(customer, customer.loyaltyPoints() + points));
    log.info(
        "loyalty points earned customerId={} orderId={} points={}", customerId, orderId, points);
  }

  /**
   * Redeems points from a customer's balance.
   *
   * @param customerId customer ID
   * @param request points to redeem
   * @return points redeemed and the remaining balance
   * @throws ValidationException if points is missing or not positive
   * @throws InsufficientPointsException if the balance is too low
   * @throws NotFoundException if the customer does not exist
   */
  public synchronized RedeemResult redeem(String customerId, RedeemRequest request) {
    if (request == null || request.points() == null || request.points() < 1) {
      throw new ValidationException("points must be a positive integer");
    }
    Customer customer = requireCustomer(customerId);
    int points = request.points();
    if (points > customer.loyaltyPoints()) {
      throw new InsufficientPointsException(
          "Customer "
              + customerId
              + " has "
              + customer.loyaltyPoints()
              + " points; cannot redeem "
              + points);
    }
    int remaining = customer.loyaltyPoints() - points;
    store.saveCustomer(withPoints(customer, remaining));
    log.info(
        "loyalty points redeemed customerId={} points={} remaining={}",
        customerId,
        points,
        remaining);
    return new RedeemResult(customerId, points, remaining);
  }

  private Customer requireCustomer(String customerId) {
    return store
        .findCustomer(customerId)
        .orElseThrow(() -> new NotFoundException("Customer " + customerId + " not found"));
  }

  private static Customer withPoints(Customer customer, int points) {
    return new Customer(customer.id(), customer.name(), customer.email(), points);
  }
}
