package com.fintex.ce.adapter.rest.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Component
@Slf4j
public class MDCInsertingServletFilter implements Filter {

  public static final String TRACE_ID = "TraceId";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    insertIntoMDC(request);
    try {
      chain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }

  void insertIntoMDC(ServletRequest request) {
    if (request instanceof HttpServletRequest) {
      HttpServletRequest httpServletRequest = (HttpServletRequest) request;
      var traceId = httpServletRequest.getHeader(TRACE_ID);
      MDC.put(TRACE_ID, nonNull(traceId) ? traceId : UUID.randomUUID().toString());
    }

  }
}