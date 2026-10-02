package com.contoso.storefront.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Tags every request with an {@code x-request-id} and logs one line per completed request. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

  /** MDC key, also used by the log pattern. */
  public static final String REQUEST_ID = "requestId";

  private static final Logger log = LoggerFactory.getLogger("http");
  private static final Pattern VALID_ID = Pattern.compile("^[\\w-]{1,64}$");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String incoming = request.getHeader("x-request-id");
    String requestId =
        incoming != null && VALID_ID.matcher(incoming).matches()
            ? incoming
            : UUID.randomUUID().toString().substring(0, 8);
    long started = System.currentTimeMillis();
    MDC.put(REQUEST_ID, requestId);
    response.setHeader("x-request-id", requestId);
    try {
      chain.doFilter(request, response);
    } finally {
      log.info(
          "request completed method={} path={} status={} durationMs={}",
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          System.currentTimeMillis() - started);
      MDC.remove(REQUEST_ID);
    }
  }
}
