package com.contoso.storefront.error;

import org.springframework.http.HttpStatus;

/** The request body is missing data or has invalid values. */
public class ValidationException extends StorefrontException {

  /**
   * Creates the exception.
   *
   * @param message client-safe description
   */
  public ValidationException(String message) {
    super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
  }
}
