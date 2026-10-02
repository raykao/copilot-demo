package com.contoso.storefront.loyalty;

import com.contoso.storefront.error.InvalidPurchaseException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

/**
 * Loyalty points earned for a purchase (specs/loyalty-points/REQUIREMENTS.md): 1 point per whole
 * dollar, plus a 10% bonus (rounded down) for purchases of $100 or more.
 */
@Component
public class LoyaltyCalculator {

  private static final BigDecimal BONUS_THRESHOLD = BigDecimal.valueOf(100);
  private static final int BONUS_PERCENT = 10;

  /**
   * Calculates points earned for a purchase.
   *
   * @param purchaseAmount order total in USD
   * @return whole, non-negative points
   * @throws InvalidPurchaseException if the amount is null, zero or negative
   */
  public int calculatePoints(BigDecimal purchaseAmount) {
    if (purchaseAmount == null || purchaseAmount.signum() <= 0) {
      throw new InvalidPurchaseException(
          "Purchase amount must be positive, got " + purchaseAmount);
    }
    int basePoints = purchaseAmount.setScale(0, RoundingMode.FLOOR).intValueExact();
    int bonusPoints =
        purchaseAmount.compareTo(BONUS_THRESHOLD) >= 0 ? basePoints * BONUS_PERCENT / 100 : 0;
    return basePoints + bonusPoints;
  }
}
