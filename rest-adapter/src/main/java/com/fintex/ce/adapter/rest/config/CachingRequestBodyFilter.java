package com.fintex.ce.adapter.rest.config;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@Component
public class CachingRequestBodyFilter extends GenericFilterBean {
  @Override
  public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
      final FilterChain chain)
      throws IOException, ServletException {
    final HttpServletRequest currentRequest = (HttpServletRequest) servletRequest;
    final ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(currentRequest);
    chain.doFilter(wrappedRequest, servletResponse);
  }
}
