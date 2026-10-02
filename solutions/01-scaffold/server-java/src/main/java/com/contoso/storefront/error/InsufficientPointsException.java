package com.contoso.storefront.error;

import org.springframework.http.HttpStatus;

/** A customer tried to redeem more loyalty points than they have. */
public class InsufficientPointsException extends StorefrontException {

  /**
   * Creates the exception.
   *
   * @param message client-safe description
   */
  public InsufficientPointsException(String message) {
    super(message, HttpStatus.CONFLICT, "INSUFFICIENT_POINTS");
  }
}
