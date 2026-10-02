package com.contoso.storefront.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import com.contoso.storefront.TestStores;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class InventoryServiceTest {

  private static final List<StockCount> COUNT_SHEET =
      List.of(
          new StockCount("SKU-1001", 40),
          new StockCount("SKU-1002", 250),
          new StockCount("SKU-1003", 130),
          new StockCount("SKU-1004", 21),
          new StockCount("SKU-1005", 64),
          new StockCount("SKU-99871", 12));

  private InventoryService inventoryService;

  @BeforeEach
  void setUp() {
    inventoryService = new InventoryService(TestStores.seeded());
  }

  @Nested
  class Reconcile {

    @Test
    void givenUnregisteredSku_whenReconciled_thenItIsSkippedAndReported() {
      var result = inventoryService.reconcile(new ReconcileRequest(COUNT_SHEET));

      assertThat(result.unknownSkus()).containsExactly("SKU-99871");
      assertThat(result.variances())
          .extracting(Variance::sku)
          .containsExactly("SKU-1001", "SKU-1004");
    }

    @Test
    void givenMixedVariances_whenReconciled_thenAverageIsMeanAbsoluteVariance() {
      var result = inventoryService.reconcile(new ReconcileRequest(COUNT_SHEET));

      assertThat(result.averageVariance()).isEqualTo(2.5);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SKU-1002", "SKU-1003"})
    void givenMatchingCountAbove127_whenReconciled_thenNoVarianceIsReported(String sku) {
      int onHand = sku.equals("SKU-1002") ? 250 : 130;

      var result =
          inventoryService.reconcile(new ReconcileRequest(List.of(new StockCount(sku, onHand))));

      assertThat(result.variances()).isEmpty();
      assertThat(result.averageVariance()).isZero();
    }
  }
}
