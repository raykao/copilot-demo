package com.contoso.storefront.error;

import org.springframework.http.HttpStatus;

/** A purchase amount is zero or negative, so no loyalty points can be calculated. */
public class InvalidPurchaseException extends StorefrontException {

  /**
   * Creates the exception.
   *
   * @param message client-safe description
   */
  public InvalidPurchaseException(String message) {
    super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
  }
}
