package com.contoso.storefront.domain;

import java.math.BigDecimal;

/**
 * A promo code that was applied to a quote.
 *
 * @param code normalized promo code
 * @param discount discount amount it produced
 */
public record AppliedPromo(String code, BigDecimal discount) {}
