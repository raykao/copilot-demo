package com.contoso.storefront.web;

import com.contoso.storefront.domain.Product;
import com.contoso.storefront.store.InMemoryStore;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Health check, product catalog and customer directory (read-only). */
@RestController
@RequestMapping("/api")
public class CatalogController {

  private final InMemoryStore store;

  /**
   * Creates the controller.
   *
   * @param store data store
   */
  public CatalogController(InMemoryStore store) {
    this.store = store;
  }

  /**
   * Customer summary without PII.
   *
   * @param id customer ID
   * @param name display name
   */
  public record CustomerSummary(String id, String name) {}

  /**
   * Health check.
   *
   * @return status and backend name
   */
  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("status", "ok", "backend", "java");
  }

  /**
   * Lists the product catalog.
   *
   * @return all products
   */
  @GetMapping("/products")
  public List<Product> products() {
    return store.listProducts();
  }

  /**
   * Lists customers.
   *
   * @return customer IDs and names
   */
  @GetMapping("/customers")
  public List<CustomerSummary> customers() {
    return store.listCustomers().stream()
        .map(c -> new CustomerSummary(c.id(), c.name()))
        .toList();
  }
}
