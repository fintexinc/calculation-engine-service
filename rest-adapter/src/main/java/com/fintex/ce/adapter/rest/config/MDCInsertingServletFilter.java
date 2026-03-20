package com.fintex.ce.adapter.rest.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

import static java.util.Objects.nonNull;

/**
 * Servlet filter that integrates incoming TraceId header with Micrometer Tracing.
 *
 * <p>When Micrometer Tracing is active, traceId and spanId are automatically propagated
 * to MDC and downstream WebClient calls. This filter additionally reads a custom
 * {@code TraceId} header from inbound requests and places it in MDC for backward
 * compatibility with existing log correlation.</p>
 */
@Component
@RequiredArgsConstructor
public class MDCInsertingServletFilter implements Filter {

  public static final String TRACE_ID = "TraceId";

  private final Tracer tracer;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    insertIntoMDC(request);
    try {
      chain.doFilter(request, response);
    } finally {
      MDC.remove(TRACE_ID);
    }
  }

  void insertIntoMDC(ServletRequest request) {
    if (request instanceof HttpServletRequest httpServletRequest) {
      String headerTraceId = httpServletRequest.getHeader(TRACE_ID);
      if (nonNull(headerTraceId)) {
        MDC.put(TRACE_ID, headerTraceId);
        return;
      }
    }

    Span currentSpan = tracer.currentSpan();
    if (nonNull(currentSpan)) {
      MDC.put(TRACE_ID, currentSpan.context().traceId());
    }
  }
}
