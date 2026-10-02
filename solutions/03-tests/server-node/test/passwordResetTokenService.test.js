import { jest } from '@jest/globals';
import { PasswordResetTokenService } from '../src/services/passwordReset/passwordResetTokenService.js';
import { InMemoryTokenStore } from '../src/services/passwordReset/inMemoryTokenStore.js';
import { Logger } from '../src/logger.js';

const START = new Date('2026-10-01T12:00:00Z');

const setup = () => {
  let now = START;
  const lines = [];
  const service = new PasswordResetTokenService({
    store: new InMemoryTokenStore(),
    clock: () => now,
    logger: new Logger('PasswordResetTokenService', (line) => lines.push(line)),
  });
  const advanceMinutes = (minutes) => {
    now = new Date(now.getTime() + minutes * 60_000);
  };
  return { service, advanceMinutes, lines };
};

describe('PasswordResetTokenService', () => {
  describe('generateResetToken', () => {
    it('should return a URL-safe token of at least 43 characters (FR1, NFR1)', () => {
      // Arrange
      const { service } = setup();

      // Act
      const token = service.generateResetToken('C-100');

      // Assert
      expect(token).toMatch(/^[A-Za-z0-9_-]{43,}$/);
    });

    it('should use the injected cryptographic random source (NFR1)', () => {
      // Arrange
      const randomBytes = jest.fn(() => Buffer.alloc(32, 7));
      const service = new PasswordResetTokenService({ randomBytes });

      // Act
      service.generateResetToken('C-100');

      // Assert
      expect(randomBytes).toHaveBeenCalledWith(32);
    });

    it('should return different tokens on each call', () => {
      // Arrange
      const { service } = setup();

      // Act
      const tokens = new Set(Array.from({ length: 20 }, () => service.generateResetToken('C-100')));

      // Assert
      expect(tokens.size).toBe(20);
    });

    it('should invalidate the previous active token for the same user (FR5)', () => {
      // Arrange
      const { service } = setup();
      const first = service.generateResetToken('C-100');

      // Act
      const second = service.generateResetToken('C-100');

      // Assert
      expect(service.validateResetToken(first)).toBeUndefined();
      expect(service.validateResetToken(second)).toBe('C-100');
    });

    it('should not invalidate tokens belonging to other users (FR5)', () => {
      // Arrange
      const { service } = setup();
      const ada = service.generateResetToken('C-100');

      // Act
      service.generateResetToken('C-101');

      // Assert
      expect(service.validateResetToken(ada)).toBe('C-100');
    });

    it('should never write the raw token to the logs (NFR2)', () => {
      // Arrange
      const { service, lines } = setup();

      // Act
      const token = service.generateResetToken('C-100');
      service.consumeResetToken(token);

      // Assert
      expect(lines.join('\n')).not.toContain(token);
    });
  });

  describe('validateResetToken', () => {
    it('should return the userId for a fresh token (FR2)', () => {
      // Arrange
      const { service } = setup();
      const token = service.generateResetToken('C-100');

      // Act
      const userId = service.validateResetToken(token);

      // Assert
      expect(userId).toBe('C-100');
    });

    it('should still accept a token one minute before it expires (FR1)', () => {
      // Arrange
      const { service, advanceMinutes } = setup();
      const token = service.generateResetToken('C-100');

      // Act
      advanceMinutes(14);

      // Assert
      expect(service.validateResetToken(token)).toBe('C-100');
    });

    it('should return undefined once 15 minutes have passed (FR1, FR3)', () => {
      // Arrange
      const { service, advanceMinutes } = setup();
      const token = service.generateResetToken('C-100');

      // Act
      advanceMinutes(15);

      // Assert
      expect(service.validateResetToken(token)).toBeUndefined();
    });

    it.each([['unknown-token'], [''], [undefined], [null]])(
      'should return undefined without throwing for %p (FR3)',
      (token) => {
        // Arrange
        const { service } = setup();

        // Act + Assert
        expect(() => service.validateResetToken(token)).not.toThrow();
        expect(service.validateResetToken(token)).toBeUndefined();
      },
    );
  });

  describe('consumeResetToken', () => {
    it('should return the userId and make the token unusable (FR4)', () => {
      // Arrange
      const { service } = setup();
      const token = service.generateResetToken('C-100');

      // Act
      const userId = service.consumeResetToken(token);

      // Assert
      expect(userId).toBe('C-100');
      expect(service.validateResetToken(token)).toBeUndefined();
    });

    it('should not allow a token to be consumed twice', () => {
      // Arrange
      const { service } = setup();
      const token = service.generateResetToken('C-100');
      service.consumeResetToken(token);

      // Act
      const second = service.consumeResetToken(token);

      // Assert
      expect(second).toBeUndefined();
    });

    it('should not consume an expired token', () => {
      // Arrange
      const { service, advanceMinutes } = setup();
      const token = service.generateResetToken('C-100');
      advanceMinutes(16);

      // Act
      const userId = service.consumeResetToken(token);

      // Assert
      expect(userId).toBeUndefined();
    });
  });
});
