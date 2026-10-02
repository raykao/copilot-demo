package com.contoso.storefront.pricing;

/**
 * One requested cart line.
 *
 * @param sku product SKU
 * @param quantity units requested
 */
public record CartItem(String sku, Integer quantity) {}
