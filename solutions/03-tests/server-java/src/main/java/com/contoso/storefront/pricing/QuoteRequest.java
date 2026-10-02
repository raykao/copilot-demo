package com.contoso.storefront.pricing;

import java.util.List;

/**
 * Body of {@code POST /api/cart/quote} and {@code POST /api/orders}.
 *
 * @param items cart lines
 * @param promoCode optional promo code
 * @param customerId optional customer ID
 */
public record QuoteRequest(List<CartItem> items, String promoCode, String customerId) {}
