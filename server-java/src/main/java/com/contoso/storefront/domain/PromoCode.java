package com.contoso.storefront.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A promo code.
 *
 * @param code upper-case code customers type in
 * @param type {@code percent} or {@code fixed}
 * @param value percentage (0-100) or fixed USD amount
 * @param minSubtotal minimum cart subtotal required, or null
 * @param expiresAt last valid day, or null if it never expires
 */
public record PromoCode(
    String code, String type, BigDecimal value, BigDecimal minSubtotal, LocalDate expiresAt) {}
