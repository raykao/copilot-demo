package com.contoso.storefront.discount;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Step definitions for specs/discount-eligibility/discount-eligibility.feature. */
public class DiscountEligibilitySteps {

  private List<DiscountTier> tiers;
  private int itemCount;
  private BigDecimal subtotal;
  private DiscountTier result;

  @Given("the store has the following discount tiers:")
  public void theStoreHasTheFollowingDiscountTiers(List<Map<String, String>> rows) {
    tiers =
        rows.stream()
            .map(
                row ->
                    new DiscountTier(
                        row.get("tier"),
                        Integer.parseInt(row.get("minimum_items")),
                        new BigDecimal(row.get("minimum_subtotal")),
                        Integer.parseInt(row.get("discount_percent"))))
            .toList();
  }

  @Given("a cart with {int} items and a subtotal of ${bigdecimal}")
  public void aCartWithItemsAndASubtotalOf(int items, BigDecimal amount) {
    itemCount = items;
    subtotal = amount;
  }

  @When("the discount tier is calculated")
  public void theDiscountTierIsCalculated() {
    result = new DiscountEligibility(tiers).determineTier(itemCount, subtotal);
  }

  @Then("the applied discount tier should be {string}")
  public void theAppliedDiscountTierShouldBe(String expected) {
    assertThat(result.name()).isEqualTo(expected);
  }

  @Then("the discount percent should be {int}")
  public void theDiscountPercentShouldBe(int expected) {
    assertThat(result.discountPercent()).isEqualTo(expected);
  }
}
