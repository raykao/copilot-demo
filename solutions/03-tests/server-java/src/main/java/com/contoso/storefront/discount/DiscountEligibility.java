package com.contoso.storefront.discount;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Picks the most generous discount tier a cart qualifies for. Driven by
 * specs/discount-eligibility/discount-eligibility.feature.
 */
public class DiscountEligibility {

  private final List<DiscountTier> tiers;

  /**
   * Creates the calculator.
   *
   * @param tiers available tiers
   */
  public DiscountEligibility(List<DiscountTier> tiers) {
    this.tiers = List.copyOf(tiers);
  }

  /**
   * Determines the applicable tier.
   *
   * @param itemCount number of items in the cart
   * @param subtotal cart subtotal
   * @return the qualifying tier with the highest discount, or {@link DiscountTier#NONE}
   */
  public DiscountTier determineTier(int itemCount, BigDecimal subtotal) {
    return tiers.stream()
        .filter(t -> itemCount >= t.minimumItems())
        .filter(t -> subtotal.compareTo(t.minimumSubtotal()) >= 0)
        .max(Comparator.comparingInt(DiscountTier::discountPercent))
        .orElse(DiscountTier.NONE);
  }
}
