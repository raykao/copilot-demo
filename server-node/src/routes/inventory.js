import { Router } from 'express';

/** Stock levels and stock-count reconciliation. */
export const inventoryRoutes = ({ inventoryService }) => {
  const router = Router();
  router.get('/inventory', (req, res) => res.json(inventoryService.list()));
  router.post('/inventory/reconcile', (req, res) => res.json(inventoryService.reconcile(req.body)));
  return router;
};
