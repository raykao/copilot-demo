package com.contoso.storefront.loyalty;

/**
 * Body of {@code POST /api/customers/{id}/loyalty/redeem}.
 *
 * @param points points to redeem
 */
public record RedeemRequest(Integer points) {}
