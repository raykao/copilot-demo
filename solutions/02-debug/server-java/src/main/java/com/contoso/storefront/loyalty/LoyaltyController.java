package com.contoso.storefront.loyalty;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Customer loyalty balance and redemption. */
@RestController
@RequestMapping("/api/customers/{customerId}/loyalty")
public class LoyaltyController {

  private final LoyaltyService loyaltyService;

  /**
   * Creates the controller.
   *
   * @param loyaltyService loyalty logic
   */
  public LoyaltyController(LoyaltyService loyaltyService) {
    this.loyaltyService = loyaltyService;
  }

  /**
   * Returns a customer's balance.
   *
   * @param customerId customer ID
   * @return the balance
   */
  @GetMapping
  public LoyaltyBalance balance(@PathVariable String customerId) {
    return loyaltyService.getBalance(customerId);
  }

  /**
   * Redeems points.
   *
   * @param customerId customer ID
   * @param request points to redeem
   * @return points redeemed and the remaining balance
   */
  @PostMapping("/redeem")
  public RedeemResult redeem(@PathVariable String customerId, @RequestBody RedeemRequest request) {
    return loyaltyService.redeem(customerId, request);
  }
}
