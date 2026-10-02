package com.contoso.storefront.orders;

import com.contoso.storefront.domain.Order;
import com.contoso.storefront.pricing.QuoteRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Order placement and history. */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

  private final OrderService orderService;

  /**
   * Creates the controller.
   *
   * @param orderService order logic
   */
  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  /**
   * Places an order.
   *
   * @param request cart lines, optional promo code and customer ID
   * @return the placed order
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Order placeOrder(@RequestBody QuoteRequest request) {
    return orderService.placeOrder(request);
  }

  /**
   * Lists orders placed since startup.
   *
   * @return all orders
   */
  @GetMapping
  public List<Order> listOrders() {
    return orderService.listOrders();
  }
}
