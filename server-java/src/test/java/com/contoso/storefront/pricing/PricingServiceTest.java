package com.contoso.storefront.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.contoso.storefront.TestStores;
import com.contoso.storefront.config.StorefrontProperties;
import com.contoso.storefront.error.InvalidPromoException;
import com.contoso.storefront.error.NotFoundException;
import com.contoso.storefront.error.ValidationException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PricingServiceTest {

  private PricingService pricingService;

  @BeforeEach
  void setUp() {
    StorefrontProperties properties =
        new StorefrontProperties("../data/seed.json", new BigDecimal("0.08"), null);
    Clock clock = Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), ZoneOffset.UTC);
    pricingService = new PricingService(TestStores.seeded(), properties, clock);
  }

  private static QuoteRequest cart(String sku, int quantity, String promoCode) {
    return new QuoteRequest(List.of(new CartItem(sku, quantity)), promoCode, null);
  }

  @Nested
  class QuoteMethod {

    @Test
    void givenSingleItemAndNoPromo_whenQuoted_thenTaxIsAddedToSubtotal() {
      var quote = pricingService.quote(cart("SKU-1001", 1, null));

      assertThat(quote.subtotal()).isEqualByComparingTo("89.99");
      assertThat(quote.discount()).isEqualByComparingTo("0");
      assertThat(quote.tax()).isEqualByComparingTo("7.20");
      assertThat(quote.total()).isEqualByComparingTo("97.19");
    }

    @Test
    void givenQuantityOfThree_whenQuoted_thenLineTotalIsUnitPriceTimesQuantity() {
      var quote = pricingService.quote(cart("SKU-1002", 3, null));

      assertThat(quote.lineItems().get(0).lineTotal()).isEqualByComparingTo("73.50");
    }

    @Test
    void givenEmptyCart_whenQuoted_thenValidationExceptionIsThrown() {
      assertThatThrownBy(() -> pricingService.quote(new QuoteRequest(List.of(), null, null)))
          .isInstanceOf(ValidationException.class);
    }

    @Test
    void givenUnknownSku_whenQuoted_thenNotFoundExceptionIsThrown() {
      assertThatThrownBy(() -> pricingService.quote(cart("SKU-0000", 1, null)))
          .isInstanceOf(NotFoundException.class);
    }

    @Test
    void givenExpiredPromo_whenQuoted_thenInvalidPromoExceptionIsThrown() {
      assertThatThrownBy(() -> pricingService.quote(cart("SKU-1001", 1, "SUMMER25")))
          .isInstanceOf(InvalidPromoException.class)
          .hasMessageContaining("expired");
    }
  }
}
