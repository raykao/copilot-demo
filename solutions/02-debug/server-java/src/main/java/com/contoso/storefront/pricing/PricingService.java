package com.contoso.storefront.pricing;

import com.contoso.storefront.config.StorefrontProperties;
import com.contoso.storefront.domain.AppliedPromo;
import com.contoso.storefront.domain.LineItem;
import com.contoso.storefront.domain.Product;
import com.contoso.storefront.domain.PromoCode;
import com.contoso.storefront.error.InvalidPromoException;
import com.contoso.storefront.error.NotFoundException;
import com.contoso.storefront.error.ValidationException;
import com.contoso.storefront.store.InMemoryStore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Prices a cart: line totals, promo discount, tax and grand total. */
@Service
public class PricingService {

  private static final Logger log = LoggerFactory.getLogger(PricingService.class);
  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
  private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2);

  private final InMemoryStore store;
  private final BigDecimal taxRate;
  private final Clock clock;

  /**
   * Creates the service.
   *
   * @param store data store
   * @param properties storefront settings (tax rate)
   * @param clock clock used for promo expiry
   */
  public PricingService(InMemoryStore store, StorefrontProperties properties, Clock clock) {
    this.store = store;
    this.taxRate = properties.taxRate();
    this.clock = clock;
  }

  /**
   * Prices a cart.
   *
   * @param request cart lines and optional promo code
   * @return the priced quote
   * @throws ValidationException if the cart is empty or a quantity is invalid
   * @throws NotFoundException if a SKU does not exist
   * @throws InvalidPromoException if the promo code cannot be applied
   */
  public Quote quote(QuoteRequest request) {
    if (request == null || request.items() == null || request.items().isEmpty()) {
      throw new ValidationException("Cart must contain at least one item");
    }

    List<LineItem> lineItems = request.items().stream().map(this::priceLine).toList();
    BigDecimal subtotal =
        lineItems.stream()
            .map(LineItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    Optional<PromoCode> promo = resolvePromo(request.promoCode(), subtotal);
    BigDecimal discount = promo.map(p -> discountFor(p, subtotal)).orElse(ZERO_MONEY);
    BigDecimal tax = subtotal.subtract(discount).multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
    BigDecimal total = subtotal.subtract(discount).add(tax);

    log.info(
        "quote computed items={} subtotal={} promo={} discount={} taxRate={} tax={} total={}",
        lineItems.size(),
        subtotal,
        promo.map(PromoCode::code).orElse("none"),
        discount,
        taxRate,
        tax,
        total);

    return new Quote(
        lineItems,
        subtotal,
        promo.map(p -> new AppliedPromo(p.code(), discount)).orElse(null),
        discount,
        tax,
        total);
  }

  private LineItem priceLine(CartItem item) {
    if (item.quantity() == null || item.quantity() < 1) {
      throw new ValidationException(
          "Quantity for " + item.sku() + " must be a positive integer");
    }
    Product product =
        store
            .findProduct(item.sku())
            .orElseThrow(() -> new NotFoundException("Product " + item.sku() + " not found"));
    if (product.price() == null) {
      log.warn("product has no price sku={}", product.sku());
      throw new ValidationException(
          "Product " + product.sku() + " has no price and cannot be purchased");
    }
    BigDecimal unitPrice = product.price().amount();
    BigDecimal lineTotal =
        unitPrice.multiply(BigDecimal.valueOf(item.quantity())).setScale(2, RoundingMode.HALF_UP);
    return new LineItem(product.sku(), product.name(), unitPrice, item.quantity(), lineTotal);
  }

  private Optional<PromoCode> resolvePromo(String code, BigDecimal subtotal) {
    if (code == null || code.isBlank()) {
      return Optional.empty();
    }
    PromoCode promo =
        store
            .findPromo(code.trim().toUpperCase(Locale.ROOT))
            .orElseThrow(() -> new InvalidPromoException("Promo code " + code + " is not valid"));
    if (promo.expiresAt() != null && promo.expiresAt().isBefore(LocalDate.now(clock))) {
      throw new InvalidPromoException("Promo code " + code + " has expired");
    }
    if (promo.minSubtotal() != null && subtotal.compareTo(promo.minSubtotal()) < 0) {
      throw new InvalidPromoException(
          "Promo code " + code + " requires a minimum subtotal of " + promo.minSubtotal());
    }
    return Optional.of(promo);
  }

  private BigDecimal discountFor(PromoCode promo, BigDecimal subtotal) {
    return switch (promo.type()) {
      case "percent" -> subtotal.multiply(promo.value()).divide(HUNDRED, 2, RoundingMode.HALF_UP);
      case "fixed" -> promo.value().min(subtotal).setScale(2, RoundingMode.HALF_UP);
      default -> ZERO_MONEY;
    };
  }
}
