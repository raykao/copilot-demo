import { Router } from 'express';

/** Cart quotes and order placement. */
export const orderRoutes = ({ pricingService, orderService }) => {
  const router = Router();
  router.post('/cart/quote', (req, res) => res.json(pricingService.quote(req.body)));
  router.post('/orders', (req, res) => res.status(201).json(orderService.placeOrder(req.body)));
  router.get('/orders', (req, res) => res.json(orderService.listOrders()));
  return router;
};
