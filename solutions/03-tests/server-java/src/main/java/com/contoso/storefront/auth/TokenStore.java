package com.contoso.storefront.auth;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Storage for reset tokens (spec NFR3), so a persistent store can replace the in-memory one. */
public interface TokenStore {

  /**
   * Saves or replaces a record.
   *
   * @param record the record
   */
  void save(ResetTokenRecord record);

  /**
   * Finds a record by token hash.
   *
   * @param tokenHash hex SHA-256 of the raw token
   * @return the record, if present
   */
  Optional<ResetTokenRecord> findByHash(String tokenHash);

  /**
   * Finds a user's active tokens.
   *
   * @param userId the user
   * @param now the current instant
   * @return unused, unexpired records for the user
   */
  List<ResetTokenRecord> findActiveByUser(String userId, Instant now);

  /**
   * Marks a token as used.
   *
   * @param tokenHash hex SHA-256 of the raw token
   * @param usedAt when it was used or revoked
   */
  void markUsed(String tokenHash, Instant usedAt);
}
