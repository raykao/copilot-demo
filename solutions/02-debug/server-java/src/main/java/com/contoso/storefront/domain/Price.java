package com.contoso.storefront.domain;

import java.math.BigDecimal;

/**
 * A monetary amount.
 *
 * @param amount the amount
 * @param currency ISO currency code, e.g. USD
 */
public record Price(BigDecimal amount, String currency) {}
