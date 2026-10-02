import { calculatePoints } from '../src/services/loyaltyCalculator.js';
import { InvalidPurchaseError } from '../src/errors.js';

// Built test-first, one requirement at a time, from specs/loyalty-points/REQUIREMENTS.md.
describe('calculatePoints', () => {
  // Requirement 1
  it('should award 1 point per whole dollar spent', () => {
    // Arrange
    const amount = 42.9;

    // Act
    const points = calculatePoints(amount);

    // Assert
    expect(points).toBe(42);
  });

  // Requirement 2
  it('should add a 10% bonus for purchases of $100 or more', () => {
    // Arrange
    const amount = 150;

    // Act
    const points = calculatePoints(amount);

    // Assert
    expect(points).toBe(165);
  });

  it.each([
    [99.99, 99],
    [100, 110],
    [109.5, 119],
  ])('should apply the bonus threshold inclusively: $%p earns %p points', (amount, expected) => {
    // Act
    const points = calculatePoints(amount);

    // Assert
    expect(points).toBe(expected);
  });

  // Requirement 3
  it.each([0, -5, Number.NaN])('should throw InvalidPurchaseError when the amount is %p', (amount) => {
    // Act + Assert
    expect(() => calculatePoints(amount)).toThrow(InvalidPurchaseError);
  });

  // Requirements 4 and 5
  it.each([0.01, 0.99, 1.5, 250.75, 1234.56])(
    'should return a whole, non-negative number of points for $%p',
    (amount) => {
      // Act
      const points = calculatePoints(amount);

      // Assert
      expect(Number.isInteger(points)).toBe(true);
      expect(points).toBeGreaterThanOrEqual(0);
    },
  );
});
