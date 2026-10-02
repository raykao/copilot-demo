package com.contoso.storefront.domain;

/**
 * A catalog product.
 *
 * @param sku stock keeping unit
 * @param name display name
 * @param category catalog category
 * @param price list price
 */
public record Product(String sku, String name, String category, Price price) {}
