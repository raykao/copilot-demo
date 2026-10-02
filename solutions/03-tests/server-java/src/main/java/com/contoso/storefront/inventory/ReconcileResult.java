package com.contoso.storefront.inventory;

import java.util.List;

/**
 * Result of a stock-count reconciliation.
 *
 * @param variances SKUs whose count differs from the system
 * @param averageVariance mean absolute variance across {@code variances}
 * @param unknownSkus counted SKUs that are not registered (skipped)
 */
public record ReconcileResult(
    List<Variance> variances, double averageVariance, List<String> unknownSkus) {}
