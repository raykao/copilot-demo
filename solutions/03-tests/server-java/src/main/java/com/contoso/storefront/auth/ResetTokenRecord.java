package com.contoso.storefront.auth;

import java.time.Instant;

/**
 * A stored reset token. Only the SHA-256 hash of the raw token is kept.
 *
 * @param tokenHash hex SHA-256 of the raw token
 * @param userId user the token belongs to
 * @param expiresAt instant after which the token is invalid
 * @param usedAt when the token was consumed or revoked, or null
 */
public record ResetTokenRecord(String tokenHash, String userId, Instant expiresAt, Instant usedAt) {

  /**
   * Whether the token can still be used.
   *
   * @param now the current instant
   * @return true if unused and not yet expired
   */
  public boolean isActive(Instant now) {
    return usedAt == null && now.isBefore(expiresAt);
  }
}
