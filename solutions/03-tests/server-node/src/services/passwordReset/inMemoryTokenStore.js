/**
 * In-memory token storage (spec NFR3). A persistent store only needs the same four methods.
 * Records are keyed by the SHA-256 hash of the token, so raw tokens are never stored.
 */
export class InMemoryTokenStore {
  #records = new Map();

  /** @param {{ tokenHash: string, userId: string, expiresAt: Date, usedAt: Date | null }} record */
  save(record) {
    this.#records.set(record.tokenHash, { ...record });
  }

  /** @param {string} tokenHash */
  findByHash(tokenHash) {
    const record = this.#records.get(tokenHash);
    return record ? { ...record } : undefined;
  }

  /**
   * @param {string} userId
   * @param {Date} now
   */
  findActiveByUser(userId, now) {
    return [...this.#records.values()]
      .filter((r) => r.userId === userId && r.usedAt === null && r.expiresAt > now)
      .map((r) => ({ ...r }));
  }

  /**
   * @param {string} tokenHash
   * @param {Date} usedAt
   */
  markUsed(tokenHash, usedAt) {
    const record = this.#records.get(tokenHash);
    if (record) record.usedAt = usedAt;
  }
}
