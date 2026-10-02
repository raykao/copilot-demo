package com.contoso.storefront.error;

import org.springframework.http.HttpStatus;

/** A promo code is unknown, expired, or not applicable to the cart. */
public class InvalidPromoException extends StorefrontException {

  /**
   * Creates the exception.
   *
   * @param message client-safe description
   */
  public InvalidPromoException(String message) {
    super(message, HttpStatus.BAD_REQUEST, "INVALID_PROMO");
  }
}
