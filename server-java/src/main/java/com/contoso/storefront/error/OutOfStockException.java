package com.contoso.storefront.error;

import org.springframework.http.HttpStatus;

/** There is not enough stock to fulfil an order line. */
public class OutOfStockException extends StorefrontException {

  /**
   * Creates the exception.
   *
   * @param message client-safe description
   */
  public OutOfStockException(String message) {
    super(message, HttpStatus.CONFLICT, "OUT_OF_STOCK");
  }
}
