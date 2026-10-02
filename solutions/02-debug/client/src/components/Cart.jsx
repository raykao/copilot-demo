import { formatMoney } from '../format.js';
import ErrorBanner from './ErrorBanner.jsx';

/** Cart lines, promo code, customer picker and the live price quote. */
export default function Cart({
  cart,
  products,
  customers,
  customerId,
  promoInput,
  quote,
  error,
  busy,
  onQuantityChange,
  onCustomerChange,
  onPromoInput,
  onApplyPromo,
  onCheckout,
}) {
  const nameFor = (sku) => products.find((p) => p.sku === sku)?.name ?? sku;

  return (
    <section className="panel cart">
      <h2>Cart</h2>

      <label className="field">
        Customer
        <select value={customerId} onChange={(e) => onCustomerChange(e.target.value)}>
          <option value="">Guest checkout</option>
          {customers.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name} ({c.id})
            </option>
          ))}
        </select>
      </label>

      {cart.length === 0 ? (
        <p className="muted">Your cart is empty.</p>
      ) : (
        <ul className="cart-lines">
          {cart.map(({ sku, quantity }) => (
            <li key={sku}>
              <span>{nameFor(sku)}</span>
              <span className="qty">
                <button type="button" aria-label="decrease" onClick={() => onQuantityChange(sku, quantity - 1)}>
                  −
                </button>
                {quantity}
                <button type="button" aria-label="increase" onClick={() => onQuantityChange(sku, quantity + 1)}>
                  +
                </button>
              </span>
            </li>
          ))}
        </ul>
      )}

      <form
        className="promo"
        onSubmit={(e) => {
          e.preventDefault();
          onApplyPromo();
        }}
      >
        <input placeholder="Promo code" value={promoInput} onChange={(e) => onPromoInput(e.target.value)} />
        <button type="submit">Apply</button>
      </form>

      <ErrorBanner error={error} />

      {quote && (
        <dl className="totals">
          <dt>Subtotal</dt>
          <dd>{formatMoney(quote.subtotal)}</dd>
          {quote.promo && (
            <>
              <dt>Promo {quote.promo.code} ✓</dt>
              <dd>−{formatMoney(quote.discount)}</dd>
            </>
          )}
          <dt>Tax (8%)</dt>
          <dd>{formatMoney(quote.tax)}</dd>
          <dt className="grand">Total</dt>
          <dd className="grand">{formatMoney(quote.total)}</dd>
        </dl>
      )}

      <button type="button" className="primary" disabled={!quote || busy} onClick={onCheckout}>
        Place order
      </button>
    </section>
  );
}
