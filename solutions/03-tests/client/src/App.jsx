import { useCallback, useEffect, useState } from 'react';
import { api } from './api/client.js';
import ProductList from './components/ProductList.jsx';
import Cart from './components/Cart.jsx';
import OrdersView from './components/OrdersView.jsx';
import InventoryView from './components/InventoryView.jsx';
import LoyaltyPanel from './components/LoyaltyPanel.jsx';
import ErrorBanner from './components/ErrorBanner.jsx';

const TABS = ['Shop', 'Orders', 'Inventory'];

export default function App() {
  const [tab, setTab] = useState('Shop');
  const [backend, setBackend] = useState('connecting…');
  const [products, setProducts] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [cart, setCart] = useState([]);
  const [customerId, setCustomerId] = useState('');
  const [promoInput, setPromoInput] = useState('');
  const [appliedPromo, setAppliedPromo] = useState('');
  const [quote, setQuote] = useState(null);
  const [quoteError, setQuoteError] = useState(null);
  const [notice, setNotice] = useState(null);
  const [busy, setBusy] = useState(false);
  const [ordersPlaced, setOrdersPlaced] = useState(0);

  useEffect(() => {
    const load = async () => {
      try {
        const health = await api.health();
        setBackend(health.backend);
        const [productList, customerList] = await Promise.all([api.products(), api.customers()]);
        setProducts(productList);
        setCustomers(customerList);
      } catch {
        setBackend('offline');
      }
    };
    load();
  }, []);

  const cartRequest = useCallback(
    () => ({ items: cart, promoCode: appliedPromo || undefined, customerId: customerId || undefined }),
    [cart, appliedPromo, customerId],
  );

  useEffect(() => {
    if (cart.length === 0) {
      setQuote(null);
      setQuoteError(null);
      return;
    }
    const refresh = async () => {
      try {
        setQuote(await api.quote(cartRequest()));
        setQuoteError(null);
      } catch (err) {
        setQuote(null);
        setQuoteError(err);
      }
    };
    refresh();
  }, [cart, cartRequest]);

  const addToCart = (sku) => {
    setNotice(null);
    setCart((lines) =>
      lines.some((line) => line.sku === sku)
        ? lines.map((line) => (line.sku === sku ? { ...line, quantity: line.quantity + 1 } : line))
        : [...lines, { sku, quantity: 1 }],
    );
  };

  const changeQuantity = (sku, quantity) =>
    setCart((lines) =>
      quantity < 1 ? lines.filter((line) => line.sku !== sku) : lines.map((line) => (line.sku === sku ? { ...line, quantity } : line)),
    );

  const checkout = async () => {
    setBusy(true);
    try {
      const order = await api.placeOrder(cartRequest());
      const earned = order.pointsEarned > 0 ? ` You earned ${order.pointsEarned} points.` : '';
      setNotice({ kind: 'success', text: `Order ${order.id} placed. Total charged: $${order.total}.${earned}` });
      setOrdersPlaced((n) => n + 1);
      setCart([]);
      setAppliedPromo('');
      setPromoInput('');
    } catch (err) {
      setNotice({ kind: 'error', error: err });
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="app">
      <header>
        <h1>Contoso Outfitters</h1>
        <nav>
          {TABS.map((name) => (
            <button key={name} type="button" className={tab === name ? 'active' : ''} onClick={() => setTab(name)}>
              {name}
            </button>
          ))}
        </nav>
        <span className={`backend-badge ${backend}`}>API: {backend}</span>
      </header>

      {notice?.kind === 'success' && <div className="success-banner">{notice.text}</div>}
      {notice?.kind === 'error' && <ErrorBanner error={notice.error} />}

      <main>
        {tab === 'Shop' && (
          <div className="shop">
            <ProductList products={products} onAdd={addToCart} />
            <div className="sidebar">
              <Cart
                cart={cart}
                products={products}
                customers={customers}
                customerId={customerId}
                promoInput={promoInput}
                quote={quote}
                error={quoteError}
                busy={busy}
                onQuantityChange={changeQuantity}
                onCustomerChange={setCustomerId}
                onPromoInput={setPromoInput}
                onApplyPromo={() => setAppliedPromo(promoInput.trim())}
                onCheckout={checkout}
              />
              <LoyaltyPanel customerId={customerId} refreshKey={ordersPlaced} />
            </div>
          </div>
        )}
        {tab === 'Orders' && <OrdersView />}
        {tab === 'Inventory' && <InventoryView />}
      </main>
    </div>
  );
}
