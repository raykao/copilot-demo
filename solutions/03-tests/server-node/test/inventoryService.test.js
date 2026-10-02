import { jest } from '@jest/globals';
import { InventoryService } from '../src/services/inventoryService.js';
import { silentLogger } from '../src/logger.js';
import { freshStore } from './helpers.js';

const countSheet = [
  { sku: 'SKU-1001', counted: 40 },
  { sku: 'SKU-1002', counted: 250 },
  { sku: 'SKU-1003', counted: 130 },
  { sku: 'SKU-1004', counted: 21 },
  { sku: 'SKU-1005', counted: 64 },
  { sku: 'SKU-99871', counted: 12 },
];

describe('InventoryService.reconcile', () => {
  it('should skip and report an unregistered SKU instead of failing the batch', () => {
    // Arrange
    const service = new InventoryService({ store: freshStore(), logger: silentLogger });

    // Act
    const result = service.reconcile({ counts: countSheet });

    // Assert
    expect(result.unknownSkus).toEqual(['SKU-99871']);
    expect(result.variances.map((v) => v.sku)).toEqual(['SKU-1001', 'SKU-1004']);
  });

  it('should log a warning for each unregistered SKU', () => {
    // Arrange
    const logger = silentLogger.child('InventoryService');
    const warn = jest.spyOn(logger, 'warn');
    const service = new InventoryService({ store: freshStore(), logger });

    // Act
    service.reconcile({ counts: countSheet });

    // Assert
    expect(warn).toHaveBeenCalledWith(expect.stringContaining('not registered'), { sku: 'SKU-99871' });
  });

  it('should average the absolute variance across SKUs that differ', () => {
    // Arrange
    const service = new InventoryService({ store: freshStore(), logger: silentLogger });

    // Act
    const result = service.reconcile({ counts: countSheet });

    // Assert
    expect(result.averageVariance).toBe(2.5);
  });

  it('should not report SKUs whose count matches the system', () => {
    // Arrange
    const service = new InventoryService({ store: freshStore(), logger: silentLogger });

    // Act
    const result = service.reconcile({ counts: [{ sku: 'SKU-1002', counted: 250 }] });

    // Assert
    expect(result.variances).toEqual([]);
    expect(result.averageVariance).toBe(0);
  });
});
