import { formatMoney } from '../format.js';

/** Product catalog grid with "Add to cart" buttons. */
export default function ProductList({ products, onAdd }) {
  return (
    <section className="panel">
      <h2>Products</h2>
      <ul className="product-grid">
        {products.map((product) => (
          <li key={product.sku} className="product-card">
            <span className="category">{product.category}</span>
            <h3>{product.name}</h3>
            <p className="sku">{product.sku}</p>
            <p className="price">{formatMoney(product.price?.amount)}</p>
            <button type="button" onClick={() => onAdd(product.sku)}>
              Add to cart
            </button>
          </li>
        ))}
      </ul>
    </section>
  );
}
