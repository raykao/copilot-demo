import { useEffect, useState } from 'react';
import { api } from '../api/client.js';
import { formatMoney } from '../format.js';
import ErrorBanner from './ErrorBanner.jsx';

/** Orders placed since the backend started. */
export default function OrdersView() {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    const load = async () => {
      try {
        setOrders(await api.orders());
      } catch (err) {
        setError(err);
      }
    };
    load();
  }, []);

  return (
    <section className="panel">
      <h2>Orders</h2>
      <ErrorBanner error={error} />
      {orders.length === 0 ? (
        <p className="muted">No orders yet.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Order</th>
              <th>Customer</th>
              <th>Items</th>
              <th>Promo</th>
              <th className="num">Total</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => (
              <tr key={order.id}>
                <td>{order.id}</td>
                <td>{order.customerId ?? 'guest'}</td>
                <td>{order.lineItems.reduce((n, line) => n + line.quantity, 0)}</td>
                <td>{order.promo?.code ?? '—'}</td>
                <td className="num">{formatMoney(order.total)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
