import express from 'express';
import { randomUUID } from 'node:crypto';
import { AppError, NotFoundError } from './errors.js';
import { requestContext } from './logger.js';
import { PricingService } from './services/pricingService.js';
import { OrderService } from './services/orderService.js';
import { InventoryService } from './services/inventoryService.js';
import { LoyaltyService } from './services/loyaltyService.js';
import { catalogRoutes } from './routes/catalog.js';
import { orderRoutes } from './routes/orders.js';
import { inventoryRoutes } from './routes/inventory.js';
import { loyaltyRoutes } from './routes/loyalty.js';

const REQUEST_ID_PATTERN = /^[\w-]{1,64}$/;

/**
 * Builds the Express app. Dependencies are injected so tests can use a fresh store and a silent logger.
 * @param {{ store: import('./store.js').Store, logger: import('./logger.js').Logger, taxRate?: number, clock?: () => Date }} deps
 */
export function createApp({ store, logger, taxRate = 0.08, clock }) {
  const pricingService = new PricingService({ store, taxRate, clock, logger: logger.child('PricingService') });
  const loyaltyService = new LoyaltyService({ store, logger: logger.child('LoyaltyService') });
  const orderService = new OrderService({
    store,
    pricingService,
    loyaltyService,
    logger: logger.child('OrderService'),
  });
  const inventoryService = new InventoryService({ store, logger: logger.child('InventoryService') });
  const httpLog = logger.child('http');
  const errorLog = logger.child('ErrorHandler');

  const app = express();
  app.disable('x-powered-by');
  app.use(express.json({ limit: '100kb' }));

  app.use((req, res, next) => {
    const incoming = req.get('x-request-id');
    const requestId = incoming && REQUEST_ID_PATTERN.test(incoming) ? incoming : randomUUID().slice(0, 8);
    const started = Date.now();
    res.set('x-request-id', requestId);
    res.on('finish', () =>
      requestContext.run({ requestId }, () =>
        httpLog.info('request completed', {
          method: req.method,
          path: req.originalUrl.split('?')[0],
          status: res.statusCode,
          durationMs: Date.now() - started,
        }),
      ),
    );
    requestContext.run({ requestId }, next);
  });

  app.get('/api/health', (req, res) => res.json({ status: 'ok', backend: 'node' }));
  app.use('/api', catalogRoutes({ store }));
  app.use('/api', orderRoutes({ pricingService, orderService }));
  app.use('/api', inventoryRoutes({ inventoryService }));
  app.use('/api', loyaltyRoutes({ loyaltyService }));

  app.use((req, res, next) => next(new NotFoundError(`No route for ${req.method} ${req.path}`)));

  app.use((err, req, res, _next) => {
    if (err instanceof AppError) {
      errorLog.warn('request rejected', { status: err.status, code: err.code, reason: err.message });
      return res.status(err.status).json({ error: { code: err.code, message: err.message } });
    }
    if (err.type === 'entity.parse.failed') {
      errorLog.warn('request rejected', { status: 400, code: 'VALIDATION_ERROR', reason: 'malformed JSON' });
      return res.status(400).json({ error: { code: 'VALIDATION_ERROR', message: 'Malformed JSON body' } });
    }
    const requestId = res.get('x-request-id');
    errorLog.error(`Unhandled error on ${req.method} ${req.originalUrl.split('?')[0]}`, {}, err);
    return res
      .status(500)
      .json({ error: { code: 'INTERNAL_ERROR', message: 'An unexpected error occurred', requestId } });
  });

  return app;
}
