import crypto from 'node:crypto';
import { ValidationError } from '../../errors.js';
import { silentLogger } from '../../logger.js';
import { InMemoryTokenStore } from './inMemoryTokenStore.js';

const TOKEN_BYTES = 32;
const hashToken = (token) => crypto.createHash('sha256').update(token).digest('hex');

/**
 * Issues and validates short-lived, single-use password reset tokens (specs/password-reset/spec.md).
 * Randomness, time and storage are injected so tests stay deterministic.
 */
export class PasswordResetTokenService {
  #store;
  #clock;
  #randomBytes;
  #log;
  #ttlMs;

  constructor({
    store = new InMemoryTokenStore(),
    clock = () => new Date(),
    randomBytes = crypto.randomBytes,
    logger = silentLogger,
    ttlMinutes = 15,
  } = {}) {
    this.#store = store;
    this.#clock = clock;
    this.#randomBytes = randomBytes;
    this.#log = logger;
    this.#ttlMs = ttlMinutes * 60_000;
  }

  /**
   * FR1 + FR5: issues a new URL-safe token and revokes any earlier active token for the user.
   * @param {string} userId
   * @returns {string} the raw token (only ever returned here, never stored or logged)
   */
  generateResetToken(userId) {
    if (typeof userId !== 'string' || userId.length === 0) {
      throw new ValidationError('userId is required');
    }
    const now = this.#clock();
    for (const active of this.#store.findActiveByUser(userId, now)) {
      this.#store.markUsed(active.tokenHash, now);
    }

    const token = this.#randomBytes(TOKEN_BYTES).toString('base64url');
    this.#store.save({
      tokenHash: hashToken(token),
      userId,
      expiresAt: new Date(now.getTime() + this.#ttlMs),
      usedAt: null,
    });
    this.#log.info('password reset token issued', { userId });
    return token;
  }

  /**
   * FR2 + FR3: returns the userId for a valid token, or undefined. Never throws for bad tokens.
   * @param {string} token
   * @returns {string | undefined}
   */
  validateResetToken(token) {
    return this.#findValid(token)?.userId;
  }

  /**
   * FR4: marks a valid token as used and returns its userId, or undefined if it isn't valid.
   * @param {string} token
   * @returns {string | undefined}
   */
  consumeResetToken(token) {
    const record = this.#findValid(token);
    if (!record) return undefined;
    this.#store.markUsed(record.tokenHash, this.#clock());
    this.#log.info('password reset token consumed', { userId: record.userId });
    return record.userId;
  }

  #findValid(token) {
    if (typeof token !== 'string' || token.length === 0) return undefined;
    const record = this.#store.findByHash(hashToken(token));
    if (!record || record.usedAt !== null || record.expiresAt <= this.#clock()) return undefined;
    return record;
  }
}
