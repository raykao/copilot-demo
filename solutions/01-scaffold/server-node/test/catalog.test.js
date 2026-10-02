import request from 'supertest';
import { createTestApp } from './helpers.js';

describe('catalog API', () => {
  it('should report the node backend as healthy', async () => {
    // Arrange
    const app = createTestApp();

    // Act
    const res = await request(app).get('/api/health');

    // Assert
    expect(res.status).toBe(200);
    expect(res.body).toEqual({ status: 'ok', backend: 'node' });
  });

  it('should list every product in the seed catalog', async () => {
    // Arrange
    const app = createTestApp();

    // Act
    const res = await request(app).get('/api/products');

    // Assert
    expect(res.status).toBe(200);
    expect(res.body).toHaveLength(6);
    expect(res.body[0]).toMatchObject({ sku: 'SKU-1001', name: 'Trail Running Shoes' });
  });

  it('should return 404 NOT_FOUND for an unknown route', async () => {
    // Arrange
    const app = createTestApp();

    // Act
    const res = await request(app).get('/api/does-not-exist');

    // Assert
    expect(res.status).toBe(404);
    expect(res.body.error.code).toBe('NOT_FOUND');
  });
});
