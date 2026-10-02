package com.contoso.storefront.loyalty;

/**
 * A customer's loyalty points balance.
 *
 * @param customerId customer ID
 * @param points current balance
 */
public record LoyaltyBalance(String customerId, int points) {}
