import { PricingService } from '../src/services/pricingService.js';
import { InvalidPromoError, ValidationError } from '../src/errors.js';
import { silentLogger } from '../src/logger.js';
import { freshStore } from './helpers.js';

const createPricing = () =>
  new PricingService({
    store: freshStore(),
    taxRate: 0.08,
    logger: silentLogger,
    clock: () => new Date('2026-10-01T12:00:00Z'),
  });

describe('PricingService.quote regressions (Module 7)', () => {
  it('should charge tax on the discounted subtotal when a promo is applied', () => {
    // Arrange
    const pricing = createPricing();

    // Act
    const quote = pricing.quote({ items: [{ sku: 'SKU-1004', quantity: 1 }], promoCode: 'SAVE10' });

    // Assert
    expect(quote.subtotal).toBe(149);
    expect(quote.discount).toBe(14.9);
    expect(quote.tax).toBe(10.73);
    expect(quote.total).toBe(144.83);
  });

  it('should throw InvalidPromoError when the promo code does not exist', () => {
    // Arrange
    const pricing = createPricing();

    // Act + Assert
    expect(() => pricing.quote({ items: [{ sku: 'SKU-1001', quantity: 1 }], promoCode: 'SAVE99' })).toThrow(
      InvalidPromoError,
    );
  });

  it('should throw ValidationError instead of crashing when a product has no price', () => {
    // Arrange
    const pricing = createPricing();

    // Act + Assert
    expect(() => pricing.quote({ items: [{ sku: 'SKU-1006', quantity: 1 }] })).toThrow(ValidationError);
  });
});
