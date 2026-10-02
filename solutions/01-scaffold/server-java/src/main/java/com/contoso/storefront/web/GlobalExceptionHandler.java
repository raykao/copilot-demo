package com.contoso.storefront.web;

import com.contoso.storefront.error.StorefrontException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Maps exceptions onto the API contract's error envelope. Stack traces only go to the log. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger("ErrorHandler");

  /**
   * Expected domain errors.
   *
   * @param ex the error
   * @return the mapped response
   */
  @ExceptionHandler(StorefrontException.class)
  public ResponseEntity<ApiError> handleStorefront(StorefrontException ex) {
    log.warn(
        "request rejected status={} code={} reason={}",
        ex.status().value(),
        ex.code(),
        ex.getMessage());
    return ResponseEntity.status(ex.status()).body(ApiError.of(ex.code(), ex.getMessage()));
  }

  /**
   * Malformed JSON bodies.
   *
   * @param ex the error
   * @return 400 VALIDATION_ERROR
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
    log.warn("request rejected status=400 code=VALIDATION_ERROR reason=malformed JSON");
    return ResponseEntity.badRequest().body(ApiError.of("VALIDATION_ERROR", "Malformed JSON body"));
  }

  /**
   * Unknown routes.
   *
   * @param ex the error
   * @param request the request
   * @return 404 NOT_FOUND
   */
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiError> handleNoRoute(
      NoResourceFoundException ex, HttpServletRequest request) {
    String message = "No route for " + request.getMethod() + " " + request.getRequestURI();
    log.warn("request rejected status=404 code=NOT_FOUND reason={}", message);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of("NOT_FOUND", message));
  }

  /**
   * Anything unexpected: log the stack trace, return a generic 500 with the request ID.
   *
   * @param ex the error
   * @param request the request
   * @return 500 INTERNAL_ERROR
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
    log.error("Unhandled error on {} {}", request.getMethod(), request.getRequestURI(), ex);
    String requestId = MDC.get(RequestIdFilter.REQUEST_ID);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            new ApiError(
                new ApiError.Body(
                    "INTERNAL_ERROR", "An unexpected error occurred", requestId)));
  }
}
