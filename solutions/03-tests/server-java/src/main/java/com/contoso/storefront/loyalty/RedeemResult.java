package com.contoso.storefront.loyalty;

/**
 * Result of a redemption.
 *
 * @param customerId customer ID
 * @param redeemed points redeemed
 * @param points remaining balance
 */
public record RedeemResult(String customerId, int redeemed, int points) {}
