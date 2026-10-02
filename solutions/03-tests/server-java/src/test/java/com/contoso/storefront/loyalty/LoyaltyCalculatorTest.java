package com.contoso.storefront.loyalty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.contoso.storefront.error.InvalidPurchaseException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Built test-first, one requirement at a time, from specs/loyalty-points/REQUIREMENTS.md. */
class LoyaltyCalculatorTest {

  private final LoyaltyCalculator calculator = new LoyaltyCalculator();

  @Nested
  class CalculatePoints {

    @Test
    void givenFractionalAmount_whenCalculated_thenOnePointPerWholeDollar() {
      assertThat(calculator.calculatePoints(new BigDecimal("42.90"))).isEqualTo(42);
    }

    @Test
    void givenAmountOver100_whenCalculated_thenTenPercentBonusIsAdded() {
      assertThat(calculator.calculatePoints(new BigDecimal("150.00"))).isEqualTo(165);
    }

    @ParameterizedTest(name = "${0} earns {1} points")
    @CsvSource({"99.99, 99", "100.00, 110", "109.50, 119"})
    void givenAmountNearThreshold_whenCalculated_thenBonusThresholdIsInclusive(
        String amount, int expected) {
      assertThat(calculator.calculatePoints(new BigDecimal(amount))).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-5.00"})
    void givenNonPositiveAmount_whenCalculated_thenInvalidPurchaseExceptionIsThrown(
        String amount) {
      assertThatThrownBy(() -> calculator.calculatePoints(new BigDecimal(amount)))
          .isInstanceOf(InvalidPurchaseException.class);
    }

    @ParameterizedTest
    @NullSource
    void givenNullAmount_whenCalculated_thenInvalidPurchaseExceptionIsThrown(BigDecimal amount) {
      assertThatThrownBy(() -> calculator.calculatePoints(amount))
          .isInstanceOf(InvalidPurchaseException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "0.99", "1.50", "250.75", "1234.56"})
    void givenAnyPositiveAmount_whenCalculated_thenPointsAreNeverNegative(String amount) {
      assertThat(calculator.calculatePoints(new BigDecimal(amount))).isNotNegative();
    }
  }
}
