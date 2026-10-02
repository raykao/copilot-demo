import { useEffect, useState } from 'react';
import { api } from '../api/client.js';
import ErrorBanner from './ErrorBanner.jsx';

/** Shows the selected customer's loyalty balance and lets them redeem points. */
export default function LoyaltyPanel({ customerId, refreshKey }) {
  const [balance, setBalance] = useState(null);
  const [points, setPoints] = useState('');
  const [error, setError] = useState(null);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    setBalance(null);
    setError(null);
    setMessage(null);
    if (!customerId) return;
    const load = async () => {
      try {
        setBalance(await api.loyalty(customerId));
      } catch (err) {
        setError(err);
      }
    };
    load();
  }, [customerId, refreshKey]);

  if (!customerId) return null;

  const redeem = async (e) => {
    e.preventDefault();
    setError(null);
    setMessage(null);
    try {
      const result = await api.redeemPoints(customerId, Number(points));
      setBalance({ customerId, points: result.points });
      setMessage(`Redeemed ${result.redeemed} points.`);
      setPoints('');
    } catch (err) {
      setError(err);
    }
  };

  return (
    <section className="panel">
      <h2>Loyalty</h2>
      <p>
        Balance: <strong>{balance ? `${balance.points} points` : '…'}</strong>
      </p>
      <form className="promo" onSubmit={redeem}>
        <input
          type="number"
          min="1"
          placeholder="Points to redeem"
          value={points}
          onChange={(e) => setPoints(e.target.value)}
        />
        <button type="submit" disabled={!points}>
          Redeem
        </button>
      </form>
      {message && <p className="muted">{message}</p>}
      <ErrorBanner error={error} />
    </section>
  );
}
