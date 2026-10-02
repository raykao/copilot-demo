package com.contoso.storefront.inventory;

/**
 * Stock level for display.
 *
 * @param sku stock keeping unit
 * @param name product name
 * @param onHand units on hand
 */
public record StockLevel(String sku, String name, int onHand) {}
