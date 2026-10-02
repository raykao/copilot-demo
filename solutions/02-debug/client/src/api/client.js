/** Error returned by the storefront API, carrying the contract's `code` and `requestId`. */
export class ApiError extends Error {
  constructor(status, { code = 'UNKNOWN', message = 'Request failed', requestId } = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.requestId = requestId;
  }
}

const request = async (path, { method = 'GET', body } = {}) => {
  const res = await fetch(`/api${path}`, {
    method,
    headers: body ? { 'Content-Type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  });
  const payload = await res.json().catch(() => null);
  if (!res.ok) {
    throw new ApiError(res.status, payload?.error ?? { message: `HTTP ${res.status}` });
  }
  return payload;
};

export const api = {
  health: () => request('/health'),
  products: () => request('/products'),
  customers: () => request('/customers'),
  inventory: () => request('/inventory'),
  orders: () => request('/orders'),
  quote: (cart) => request('/cart/quote', { method: 'POST', body: cart }),
  placeOrder: (cart) => request('/orders', { method: 'POST', body: cart }),
  reconcile: (counts) => request('/inventory/reconcile', { method: 'POST', body: { counts } }),
  loyalty: (customerId) => request(`/customers/${encodeURIComponent(customerId)}/loyalty`),
  redeemPoints: (customerId, points) =>
    request(`/customers/${encodeURIComponent(customerId)}/loyalty/redeem`, { method: 'POST', body: { points } }),
};
