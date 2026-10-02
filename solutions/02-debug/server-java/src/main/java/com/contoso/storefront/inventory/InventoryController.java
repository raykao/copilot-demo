package com.contoso.storefront.inventory;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Stock levels and stock-count reconciliation. */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

  private final InventoryService inventoryService;

  /**
   * Creates the controller.
   *
   * @param inventoryService inventory logic
   */
  public InventoryController(InventoryService inventoryService) {
    this.inventoryService = inventoryService;
  }

  /**
   * Lists current stock levels.
   *
   * @return stock per SKU
   */
  @GetMapping
  public List<StockLevel> list() {
    return inventoryService.list();
  }

  /**
   * Reconciles a physical count.
   *
   * @param request physical counts
   * @return variances and the average variance
   */
  @PostMapping("/reconcile")
  public ReconcileResult reconcile(@RequestBody ReconcileRequest request) {
    return inventoryService.reconcile(request);
  }
}
