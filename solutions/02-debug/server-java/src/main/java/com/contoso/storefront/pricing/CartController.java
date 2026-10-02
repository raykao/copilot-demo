package com.contoso.storefront.pricing;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Cart quotes. */
@RestController
@RequestMapping("/api/cart")
public class CartController {

  private final PricingService pricingService;

  /**
   * Creates the controller.
   *
   * @param pricingService pricing logic
   */
  public CartController(PricingService pricingService) {
    this.pricingService = pricingService;
  }

  /**
   * Prices a cart without placing an order.
   *
   * @param request cart lines and optional promo code
   * @return the quote
   */
  @PostMapping("/quote")
  public Quote quote(@RequestBody QuoteRequest request) {
    return pricingService.quote(request);
  }
}
