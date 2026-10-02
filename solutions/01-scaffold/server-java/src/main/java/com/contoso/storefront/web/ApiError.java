package com.contoso.storefront.web;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Error envelope from the API contract: {@code { "error": { "code", "message", "requestId" } }}.
 *
 * @param error error details
 */
public record ApiError(Body error) {

  /**
   * Error details.
   *
   * @param code machine-readable code
   * @param message client-safe message
   * @param requestId correlation ID, only for 500s
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record Body(String code, String message, String requestId) {}

  /**
   * Builds an error without a request ID.
   *
   * @param code machine-readable code
   * @param message client-safe message
   * @return the envelope
   */
  public static ApiError of(String code, String message) {
    return new ApiError(new Body(code, message, null));
  }
}
