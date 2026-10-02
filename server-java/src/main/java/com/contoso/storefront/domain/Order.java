package com.contoso.storefront.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * A placed order.
 *
 * @param id order ID, e.g. ORD-1001
 * @param customerId customer who placed it, or null for guests
 * @param lineItems priced lines
 * @param subtotal sum of line totals
 * @param promo applied promo, or null
 * @param discount discount amount
 * @param tax tax charged
 * @param total amount charged
 * @param createdAt when the order was placed
 */
public record Order(
    String id,
    String customerId,
    List<LineItem> lineItems,
    BigDecimal subtotal,
    AppliedPromo promo,
    BigDecimal discount,
    BigDecimal tax,
    BigDecimal total,
    Instant createdAt) {}
