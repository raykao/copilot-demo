package com.contoso.storefront.domain;

/**
 * A customer account.
 *
 * @param id customer ID
 * @param name display name
 * @param email contact email (PII: never log it)
 * @param loyaltyPoints loyalty points balance
 */
public record Customer(String id, String name, String email, int loyaltyPoints) {}
