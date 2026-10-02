package com.contoso.storefront.domain;

import java.math.BigDecimal;

/**
 * A priced cart line.
 *
 * @param sku stock keeping unit
 * @param name product name
 * @param unitPrice price per unit
 * @param quantity units ordered
 * @param lineTotal unitPrice × quantity
 */
public record LineItem(
    String sku, String name, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {}
