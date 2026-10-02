package com.contoso.storefront.inventory;

import java.util.List;

/**
 * Result of a stock-count reconciliation.
 *
 * @param variances SKUs whose count differs from the system
 * @param averageVariance average variance across {@code variances}
 */
public record ReconcileResult(List<Variance> variances, double averageVariance) {}
