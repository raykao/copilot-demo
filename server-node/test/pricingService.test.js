import { PricingService } from '../src/services/pricingService.js';
import { InvalidPromoError, NotFoundError, ValidationError } from '../src/errors.js';
import { silentLogger } from '../src/logger.js';
import { freshStore } from './helpers.js';

const createPricing = () =>
  new PricingService({
    store: freshStore(),
    taxRate: 0.08,
    logger: silentLogger,
    clock: () => new Date('2026-10-01T12:00:00Z'),
  });

describe('PricingService.quote', () => {
  it('should price a single line with tax when no promo is applied', () => {
    // Arrange
    const pricing = createPricing();

    // Act
    const quote = pricing.quote({ items: [{ sku: 'SKU-1001', quantity: 1 }] });

    // Assert
    expect(quote.subtotal).toBe(89.99);
    expect(quote.discount).toBe(0);
    expect(quote.tax).toBe(7.2);
    expect(quote.total).toBe(97.19);
  });

  it('should multiply unit price by quantity for each line', () => {
    // Arrange
    const pricing = createPricing();

    // Act
    const quote = pricing.quote({ items: [{ sku: 'SKU-1002', quantity: 3 }] });

    // Assert
    expect(quote.lineItems[0]).toMatchObject({ unitPrice: 24.5, quantity: 3, lineTotal: 73.5 });
  });

  it('should throw ValidationError when the cart is empty', () => {
    // Arrange
    const pricing = createPricing();

    // Act + Assert
    expect(() => pricing.quote({ items: [] })).toThrow(ValidationError);
  });

  it('should throw NotFoundError when a SKU does not exist', () => {
    // Arrange
    const pricing = createPricing();

    // Act + Assert
    expect(() => pricing.quote({ items: [{ sku: 'SKU-0000', quantity: 1 }] })).toThrow(NotFoundError);
  });

  it('should throw InvalidPromoError when the promo code has expired', () => {
    // Arrange
    const pricing = createPricing();

    // Act + Assert
    expect(() => pricing.quote({ items: [{ sku: 'SKU-1001', quantity: 1 }], promoCode: 'SUMMER25' })).toThrow(
      InvalidPromoError,
    );
  });
});
