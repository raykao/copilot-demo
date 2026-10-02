package com.contoso.storefront.store;

import com.contoso.storefront.domain.Customer;
import com.contoso.storefront.domain.InventoryItem;
import com.contoso.storefront.domain.Product;
import com.contoso.storefront.domain.PromoCode;
import java.util.List;

/**
 * Shape of {@code data/seed.json}, shared with the Node backend.
 *
 * @param products product catalog
 * @param inventory stock on hand per SKU
 * @param promos promo codes
 * @param customers customer accounts
 */
public record SeedData(
    List<Product> products,
    List<InventoryItem> inventory,
    List<PromoCode> promos,
    List<Customer> customers) {}
