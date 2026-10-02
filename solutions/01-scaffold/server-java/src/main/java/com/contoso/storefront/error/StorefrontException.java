package com.contoso.storefront.error;

import org.springframework.http.HttpStatus;

/** Base class for expected, client-facing errors mapped straight onto the HTTP response. */
public abstract class StorefrontException extends RuntimeException {

  private final HttpStatus status;
  private final String code;

  protected StorefrontException(String message, HttpStatus status, String code) {
    super(message);
    this.status = status;
    this.code = code;
  }

  /**
   * HTTP status to return.
   *
   * @return the status
   */
  public HttpStatus status() {
    return status;
  }

  /**
   * Machine-readable error code from the API contract.
   *
   * @return the code, e.g. INVALID_PROMO
   */
  public String code() {
    return code;
  }
}
