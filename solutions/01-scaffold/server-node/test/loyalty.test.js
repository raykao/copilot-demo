import request from 'supertest';
import { createTestApp } from './helpers.js';

describe('loyalty API', () => {
  it('should return the seeded balance for an existing customer', async () => {
    // Arrange
    const app = createTestApp();

    // Act
    const res = await request(app).get('/api/customers/C-100/loyalty');

    // Assert
    expect(res.status).toBe(200);
    expect(res.body).toEqual({ customerId: 'C-100', points: 120 });
  });

  it('should return 404 NOT_FOUND for an unknown customer', async () => {
    // Arrange
    const app = createTestApp();

    // Act
    const res = await request(app).get('/api/customers/C-999/loyalty');

    // Assert
    expect(res.status).toBe(404);
    expect(res.body.error.code).toBe('NOT_FOUND');
  });

  it('should deduct redeemed points and return the remaining balance', async () => {
    // Arrange
    const app = createTestApp();

    // Act
    const res = await request(app).post('/api/customers/C-102/loyalty/redeem').send({ points: 55 });

    // Assert
    expect(res.status).toBe(200);
    expect(res.body).toEqual({ customerId: 'C-102', redeemed: 55, points: 400 });
  });

  it('should return 409 INSUFFICIENT_POINTS when redeeming more than the balance', async () => {
    // Arrange
    const app = createTestApp();

    // Act
    const res = await request(app).post('/api/customers/C-101/loyalty/redeem').send({ points: 10 });

    // Assert
    expect(res.status).toBe(409);
    expect(res.body.error.code).toBe('INSUFFICIENT_POINTS');
  });

  it('should return 400 VALIDATION_ERROR when points is not a positive integer', async () => {
    // Arrange
    const app = createTestApp();

    // Act
    const res = await request(app).post('/api/customers/C-100/loyalty/redeem').send({ points: 0 });

    // Assert
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('VALIDATION_ERROR');
  });

  it('should credit points earned on an order to the customer', async () => {
    // Arrange
    const app = createTestApp();

    // Act
    const order = await request(app)
      .post('/api/orders')
      .send({ items: [{ sku: 'SKU-1001', quantity: 1 }], customerId: 'C-101' });
    const balance = await request(app).get('/api/customers/C-101/loyalty');

    // Assert
    expect(order.status).toBe(201);
    expect(order.body.pointsEarned).toBe(97);
    expect(balance.body.points).toBe(97);
  });

  it('should earn no points for a guest checkout', async () => {
    // Arrange
    const app = createTestApp();

    // Act
    const order = await request(app).post('/api/orders').send({ items: [{ sku: 'SKU-1001', quantity: 1 }] });

    // Assert
    expect(order.status).toBe(201);
    expect(order.body.pointsEarned).toBe(0);
  });
});
