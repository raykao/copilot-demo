package com.contoso.storefront.error;

import org.springframework.http.HttpStatus;

/** A referenced product, customer or route does not exist. */
public class NotFoundException extends StorefrontException {

  /**
   * Creates the exception.
   *
   * @param message client-safe description
   */
  public NotFoundException(String message) {
    super(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
  }
}
