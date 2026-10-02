import { useEffect, useState } from 'react';
import { api } from '../api/client.js';
import ErrorBanner from './ErrorBanner.jsx';

// Today's warehouse count sheet. SKU-99871 was found on a shelf but was never registered.
const DEFAULT_COUNT_SHEET = `SKU-1001,40
SKU-1002,250
SKU-1003,130
SKU-1004,21
SKU-1005,64
SKU-99871,12`;

const parseCountSheet = (text) =>
  text
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => {
      const [sku, counted] = line.split(',').map((part) => part.trim());
      return { sku, counted: Number(counted) };
    });

/** Stock levels plus the stock-count reconciliation tool. */
export default function InventoryView() {
  const [stock, setStock] = useState([]);
  const [sheet, setSheet] = useState(DEFAULT_COUNT_SHEET);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const loadStock = async () => {
    try {
      setStock(await api.inventory());
    } catch (err) {
      setError(err);
    }
  };

  useEffect(() => {
    loadStock();
  }, []);

  const reconcile = async () => {
    setError(null);
    setResult(null);
    try {
      setResult(await api.reconcile(parseCountSheet(sheet)));
    } catch (err) {
      setError(err);
    }
  };

  return (
    <div className="inventory">
      <section className="panel">
        <h2>Stock on hand</h2>
        <table>
          <thead>
            <tr>
              <th>SKU</th>
              <th>Product</th>
              <th className="num">On hand</th>
            </tr>
          </thead>
          <tbody>
            {stock.map((row) => (
              <tr key={row.sku}>
                <td>{row.sku}</td>
                <td>{row.name}</td>
                <td className="num">{row.onHand}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="panel">
        <h2>Reconcile stock count</h2>
        <p className="muted">One line per SKU: <code>SKU,counted</code></p>
        <textarea rows={7} value={sheet} onChange={(e) => setSheet(e.target.value)} />
        <button type="button" className="primary" onClick={reconcile}>
          Reconcile
        </button>
        <ErrorBanner error={error} />
        {result && (
          <>
            <table>
              <thead>
                <tr>
                  <th>SKU</th>
                  <th className="num">Expected</th>
                  <th className="num">Counted</th>
                  <th className="num">Variance</th>
                </tr>
              </thead>
              <tbody>
                {result.variances.map((v) => (
                  <tr key={v.sku}>
                    <td>{v.sku}</td>
                    <td className="num">{v.expected}</td>
                    <td className="num">{v.counted}</td>
                    <td className="num">{v.variance}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            <p>
              Average variance: <strong>{result.averageVariance}</strong>
            </p>
            {result.unknownSkus?.length > 0 && (
              <p className="warning">Unregistered SKUs skipped: {result.unknownSkus.join(', ')}</p>
            )}
          </>
        )}
      </section>
    </div>
  );
}
