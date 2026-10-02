package com.contoso.storefront.loyalty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

/**
 * Loyalty points earning rule. Kept separate so it can evolve on its own (see
 * specs/loyalty-points/REQUIREMENTS.md). v1: 1 point per whole dollar spent.
 */
@Component
public class LoyaltyCalculator {

  /**
   * Calculates points earned for a purchase.
   *
   * @param purchaseAmount order total in USD
   * @return points earned
   */
  public int calculatePoints(BigDecimal purchaseAmount) {
    return purchaseAmount.setScale(0, RoundingMode.FLOOR).intValueExact();
  }
}
