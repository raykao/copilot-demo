package com.contoso.storefront.pricing;

import com.contoso.storefront.domain.AppliedPromo;
import com.contoso.storefront.domain.LineItem;
import java.math.BigDecimal;
import java.util.List;

/**
 * A priced cart.
 *
 * @param lineItems priced lines
 * @param subtotal sum of line totals
 * @param promo applied promo, or null
 * @param discount discount amount
 * @param tax tax charged
 * @param total amount to charge
 */
public record Quote(
    List<LineItem> lineItems,
    BigDecimal subtotal,
    AppliedPromo promo,
    BigDecimal discount,
    BigDecimal tax,
    BigDecimal total) {}
