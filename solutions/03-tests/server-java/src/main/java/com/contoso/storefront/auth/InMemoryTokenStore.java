package com.contoso.storefront.auth;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory {@link TokenStore}. Resets on restart. */
public class InMemoryTokenStore implements TokenStore {

  private final Map<String, ResetTokenRecord> records = new ConcurrentHashMap<>();

  @Override
  public void save(ResetTokenRecord record) {
    records.put(record.tokenHash(), record);
  }

  @Override
  public Optional<ResetTokenRecord> findByHash(String tokenHash) {
    return Optional.ofNullable(records.get(tokenHash));
  }

  @Override
  public List<ResetTokenRecord> findActiveByUser(String userId, Instant now) {
    return records.values().stream()
        .filter(r -> r.userId().equals(userId) && r.isActive(now))
        .toList();
  }

  @Override
  public void markUsed(String tokenHash, Instant usedAt) {
    records.computeIfPresent(
        tokenHash, (hash, r) -> new ResetTokenRecord(hash, r.userId(), r.expiresAt(), usedAt));
  }
}
