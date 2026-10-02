package com.contoso.storefront.inventory;

import com.contoso.storefront.domain.Product;
import com.contoso.storefront.error.ValidationException;
import com.contoso.storefront.store.InMemoryStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Compares physical stock counts against the system of record. */
@Service
public class InventoryService {

  private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

  private final InMemoryStore store;

  /**
   * Creates the service.
   *
   * @param store data store
   */
  public InventoryService(InMemoryStore store) {
    this.store = store;
  }

  /**
   * Lists current stock levels.
   *
   * @return stock per SKU
   */
  public List<StockLevel> list() {
    return store.stockLevels().entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(
            e ->
                new StockLevel(
                    e.getKey(),
                    store.findProduct(e.getKey()).map(Product::name).orElse(e.getKey()),
                    e.getValue()))
        .toList();
  }

  /**
   * Reconciles a physical count against the system of record.
   *
   * @param request physical counts
   * @return variances and the average variance
   * @throws ValidationException if counts are missing or negative
   */
  public ReconcileResult reconcile(ReconcileRequest request) {
    if (request == null || request.counts() == null || request.counts().isEmpty()) {
      throw new ValidationException("counts must be a non-empty array");
    }
    for (StockCount count : request.counts()) {
      if (count.counted() == null || count.counted() < 0) {
        throw new ValidationException(
            "Counted quantity for " + count.sku() + " must be a non-negative integer");
      }
    }

    log.info("reconciliation started lines={}", request.counts().size());

    Map<String, Integer> stock = store.stockLevels();
    List<Variance> variances = new ArrayList<>();
    List<String> unknownSkus = new ArrayList<>();
    for (StockCount count : request.counts()) {
      Integer expected = stock.get(count.sku());
      if (expected == null) {
        log.warn("counted SKU is not registered; skipping sku={}", count.sku());
        unknownSkus.add(count.sku());
        continue;
      }
      int counted = count.counted();
      if (expected != counted) {
        variances.add(new Variance(count.sku(), expected, counted, counted - expected));
      }
    }

    double averageVariance =
        variances.stream().mapToInt(v -> Math.abs(v.variance())).average().orElse(0);
    averageVariance = Math.round(averageVariance * 100) / 100.0;

    log.info(
        "reconciliation complete lines={} variances={} unknownSkus={} averageVariance={}",
        request.counts().size(),
        variances.size(),
        unknownSkus.size(),
        averageVariance);
    return new ReconcileResult(variances, averageVariance, unknownSkus);
  }
}
