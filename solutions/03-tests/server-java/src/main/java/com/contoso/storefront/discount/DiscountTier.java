package com.contoso.storefront.discount;

import java.math.BigDecimal;

/**
 * A discount tier and the thresholds a cart must meet (both inclusive).
 *
 * @param name tier name, e.g. bulk
 * @param minimumItems minimum number of items in the cart
 * @param minimumSubtotal minimum cart subtotal
 * @param discountPercent discount granted by the tier
 */
public record DiscountTier(
    String name, int minimumItems, BigDecimal minimumSubtotal, int discountPercent) {

  /** The tier applied when no other tier qualifies. */
  public static final DiscountTier NONE = new DiscountTier("none", 0, BigDecimal.ZERO, 0);
}
