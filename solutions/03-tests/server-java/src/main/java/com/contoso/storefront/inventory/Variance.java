package com.contoso.storefront.inventory;

/**
 * Difference between the system of record and a physical count.
 *
 * @param sku stock keeping unit
 * @param expected units on hand according to the system
 * @param counted units counted
 * @param variance counted − expected
 */
public record Variance(String sku, Integer expected, Integer counted, int variance) {}
