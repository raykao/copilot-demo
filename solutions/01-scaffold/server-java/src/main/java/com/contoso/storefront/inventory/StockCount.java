package com.contoso.storefront.inventory;

/**
 * A physical count for one SKU.
 *
 * @param sku stock keeping unit
 * @param counted units counted on the shelf
 */
public record StockCount(String sku, Integer counted) {}
