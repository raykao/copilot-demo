import { Router } from 'express';

/** Customer loyalty balance and redemption. */
export const loyaltyRoutes = ({ loyaltyService }) => {
  const router = Router();
  router.get('/customers/:id/loyalty', (req, res) => res.json(loyaltyService.getBalance(req.params.id)));
  router.post('/customers/:id/loyalty/redeem', (req, res) => res.json(loyaltyService.redeem(req.params.id, req.body)));
  return router;
};
