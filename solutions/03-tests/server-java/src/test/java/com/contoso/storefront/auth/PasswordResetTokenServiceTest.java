package com.contoso.storefront.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.contoso.storefront.error.ValidationException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests traced to specs/password-reset/plan.md. */
class PasswordResetTokenServiceTest {

  /** Clock whose time can be moved forward. */
  static final class MutableClock extends Clock {
    private Instant now = Instant.parse("2026-10-01T12:00:00Z");

    void advance(Duration duration) {
      now = now.plus(duration);
    }

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return now;
    }
  }

  private MutableClock clock;
  private PasswordResetTokenService service;

  @BeforeEach
  void setUp() {
    clock = new MutableClock();
    service = new PasswordResetTokenService(new InMemoryTokenStore(), clock, new SecureRandom());
  }

  @Nested
  class GenerateResetToken {

    @Test
    void givenUser_whenTokenGenerated_thenItIsUrlSafeAndLongEnough() {
      String token = service.generateResetToken("C-100");

      assertThat(token).matches("^[A-Za-z0-9_-]{43,}$");
    }

    @Test
    void givenSecureRandom_whenTokenGenerated_thenItIsUsedForTokenBytes() {
      SecureRandom random = spy(new SecureRandom());
      var svc = new PasswordResetTokenService(new InMemoryTokenStore(), clock, random);

      svc.generateResetToken("C-100");

      verify(random).nextBytes(any(byte[].class));
    }

    @Test
    void givenManyCalls_whenTokensGenerated_thenAllAreUnique() {
      Set<String> tokens = new HashSet<>();
      IntStream.range(0, 20).forEach(i -> tokens.add(service.generateResetToken("C-100")));

      assertThat(tokens).hasSize(20);
    }

    @Test
    void givenActiveToken_whenNewTokenGenerated_thenPreviousIsInvalidated() {
      String first = service.generateResetToken("C-100");

      String second = service.generateResetToken("C-100");

      assertThat(service.validateResetToken(first)).isEmpty();
      assertThat(service.validateResetToken(second)).contains("C-100");
    }

    @Test
    void givenOtherUsersToken_whenNewTokenGenerated_thenOtherTokenStaysValid() {
      String ada = service.generateResetToken("C-100");

      service.generateResetToken("C-101");

      assertThat(service.validateResetToken(ada)).contains("C-100");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void givenBlankUserId_whenTokenGenerated_thenValidationExceptionIsThrown(String userId) {
      assertThatThrownBy(() -> service.generateResetToken(userId))
          .isInstanceOf(ValidationException.class);
    }
  }

  @Nested
  class ValidateResetToken {

    @Test
    void givenFreshToken_whenValidated_thenUserIdIsReturned() {
      String token = service.generateResetToken("C-100");

      assertThat(service.validateResetToken(token)).contains("C-100");
    }

    @Test
    void givenTokenOneMinuteBeforeExpiry_whenValidated_thenUserIdIsReturned() {
      String token = service.generateResetToken("C-100");

      clock.advance(Duration.ofMinutes(14));

      assertThat(service.validateResetToken(token)).contains("C-100");
    }

    @Test
    void givenTokenAtExpiry_whenValidated_thenEmptyIsReturned() {
      String token = service.generateResetToken("C-100");

      clock.advance(Duration.ofMinutes(15));

      assertThat(service.validateResetToken(token)).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"unknown-token"})
    void givenUnknownOrBlankToken_whenValidated_thenEmptyIsReturnedWithoutThrowing(String token) {
      assertThat(service.validateResetToken(token)).isEmpty();
    }
  }

  @Nested
  class ConsumeResetToken {

    @Test
    void givenValidToken_whenConsumed_thenUserIdIsReturnedAndTokenIsUnusable() {
      String token = service.generateResetToken("C-100");

      assertThat(service.consumeResetToken(token)).contains("C-100");
      assertThat(service.validateResetToken(token)).isEmpty();
    }

    @Test
    void givenConsumedToken_whenConsumedAgain_thenEmptyIsReturned() {
      String token = service.generateResetToken("C-100");
      service.consumeResetToken(token);

      assertThat(service.consumeResetToken(token)).isEmpty();
    }

    @Test
    void givenExpiredToken_whenConsumed_thenEmptyIsReturned() {
      String token = service.generateResetToken("C-100");
      clock.advance(Duration.ofMinutes(16));

      assertThat(service.consumeResetToken(token)).isEmpty();
    }
  }
}
