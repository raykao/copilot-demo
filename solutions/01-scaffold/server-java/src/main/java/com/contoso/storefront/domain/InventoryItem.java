package com.contoso.storefront.domain;

/**
 * Stock on hand for one SKU, as stored in the seed file.
 *
 * @param sku stock keeping unit
 * @param onHand units on hand
 */
public record InventoryItem(String sku, Integer onHand) {}
