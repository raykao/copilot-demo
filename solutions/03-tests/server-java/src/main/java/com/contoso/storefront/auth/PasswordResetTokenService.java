package com.contoso.storefront.auth;

import com.contoso.storefront.error.ValidationException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Issues and validates short-lived, single-use password reset tokens
 * (specs/password-reset/spec.md). Randomness, time and storage are injected so tests stay
 * deterministic.
 */
public class PasswordResetTokenService {

  private static final Logger log = LoggerFactory.getLogger(PasswordResetTokenService.class);
  private static final int TOKEN_BYTES = 32;
  private static final Duration TTL = Duration.ofMinutes(15);

  private final TokenStore store;
  private final Clock clock;
  private final SecureRandom random;

  /**
   * Creates the service.
   *
   * @param store token storage
   * @param clock time source
   * @param random cryptographically secure random source
   */
  public PasswordResetTokenService(TokenStore store, Clock clock, SecureRandom random) {
    this.store = store;
    this.clock = clock;
    this.random = random;
  }

  /**
   * FR1 + FR5: issues a new URL-safe token and revokes any earlier active token for the user.
   *
   * @param userId the user
   * @return the raw token (only ever returned here, never stored or logged)
   * @throws ValidationException if userId is blank
   */
  public String generateResetToken(String userId) {
    if (userId == null || userId.isBlank()) {
      throw new ValidationException("userId is required");
    }
    Instant now = clock.instant();
    store.findActiveByUser(userId, now).forEach(r -> store.markUsed(r.tokenHash(), now));

    byte[] bytes = new byte[TOKEN_BYTES];
    random.nextBytes(bytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    store.save(new ResetTokenRecord(hash(token), userId, now.plus(TTL), null));
    log.info("password reset token issued userId={}", userId);
    return token;
  }

  /**
   * FR2 + FR3: returns the user for a valid token. Never throws for bad tokens.
   *
   * @param token the raw token
   * @return the userId, or empty if unknown, expired or used
   */
  public Optional<String> validateResetToken(String token) {
    return findActive(token).map(ResetTokenRecord::userId);
  }

  /**
   * FR4: marks a valid token as used.
   *
   * @param token the raw token
   * @return the userId, or empty if the token isn't valid
   */
  public Optional<String> consumeResetToken(String token) {
    Optional<ResetTokenRecord> record = findActive(token);
    record.ifPresent(
        r -> {
          store.markUsed(r.tokenHash(), clock.instant());
          log.info("password reset token consumed userId={}", r.userId());
        });
    return record.map(ResetTokenRecord::userId);
  }

  private Optional<ResetTokenRecord> findActive(String token) {
    if (token == null || token.isEmpty()) {
      return Optional.empty();
    }
    Instant now = clock.instant();
    return store.findByHash(hash(token)).filter(r -> r.isActive(now));
  }

  private static String hash(String token) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
