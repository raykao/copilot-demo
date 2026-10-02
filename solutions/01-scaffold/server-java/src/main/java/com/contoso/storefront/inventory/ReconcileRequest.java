package com.contoso.storefront.inventory;

import java.util.List;

/**
 * Body of {@code POST /api/inventory/reconcile}.
 *
 * @param counts physical counts
 */
public record ReconcileRequest(List<StockCount> counts) {}
