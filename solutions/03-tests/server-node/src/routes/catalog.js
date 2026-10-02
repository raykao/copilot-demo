import { Router } from 'express';

/** Product catalog and customer directory (read-only). */
export const catalogRoutes = ({ store }) => {
  const router = Router();
  router.get('/products', (req, res) => res.json(store.listProducts()));
  router.get('/customers', (req, res) => res.json(store.listCustomers().map(({ id, name }) => ({ id, name }))));
  return router;
};
