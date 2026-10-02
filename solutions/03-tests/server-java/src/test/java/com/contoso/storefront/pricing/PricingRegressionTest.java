package com.contoso.storefront.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.contoso.storefront.TestStores;
import com.contoso.storefront.config.StorefrontProperties;
import com.contoso.storefront.error.InvalidPromoException;
import com.contoso.storefront.error.ValidationException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Regression tests for the defects fixed in Module 7. */
class PricingRegressionTest {

  private PricingService pricingService;

  @BeforeEach
  void setUp() {
    StorefrontProperties properties =
        new StorefrontProperties("../data/seed.json", new BigDecimal("0.08"), null);
    Clock clock = Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), ZoneOffset.UTC);
    pricingService = new PricingService(TestStores.seeded(), properties, clock);
  }

  private static QuoteRequest cart(String sku, String promoCode) {
    return new QuoteRequest(List.of(new CartItem(sku, 1)), promoCode, null);
  }

  @Test
  void givenPercentPromo_whenQuoted_thenTaxIsChargedOnDiscountedSubtotal() {
    var quote = pricingService.quote(cart("SKU-1004", "SAVE10"));

    assertThat(quote.subtotal()).isEqualByComparingTo("149.00");
    assertThat(quote.discount()).isEqualByComparingTo("14.90");
    assertThat(quote.tax()).isEqualByComparingTo("10.73");
    assertThat(quote.total()).isEqualByComparingTo("144.83");
  }

  @Test
  void givenUnknownPromo_whenQuoted_thenInvalidPromoExceptionIsThrown() {
    assertThatThrownBy(() -> pricingService.quote(cart("SKU-1001", "SAVE99")))
        .isInstanceOf(InvalidPromoException.class);
  }

  @Test
  void givenProductWithoutPrice_whenQuoted_thenValidationExceptionIsThrown() {
    assertThatThrownBy(() -> pricingService.quote(cart("SKU-1006", null)))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("SKU-1006");
  }
}
