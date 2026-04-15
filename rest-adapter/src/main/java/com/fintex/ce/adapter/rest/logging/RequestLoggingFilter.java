package com.fintex.ce.adapter.rest.logging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import org.slf4j.MDC;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.fintex.ce.adapter.rest.util.HttpConstants.REQUEST_ID_HEADER;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@ConditionalOnProperty(name = "logging.http.enabled", havingValue = "true", matchIfMissing = true)
public class RequestLoggingFilter extends OncePerRequestFilter {

  private static final String REQUEST_ID_ATTRIBUTE = "requestId";

  private final HttpLoggingProperties properties;
  private final SensitiveDataMasker masker;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    if (shouldSkip(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    String requestId = getOrCreateRequestId(request);
    request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
    response.setHeader(REQUEST_ID_HEADER, requestId);
    MDC.put(REQUEST_ID_ATTRIBUTE, requestId);

    long startTime = System.currentTimeMillis();

    // Bodies are logged at TRACE only — skip buffering when TRACE is off.
    boolean traceEnabled = log.isTraceEnabled();
    boolean wrapRequest = traceEnabled && shouldCacheRequestBody(request);
    boolean wrapResponse = traceEnabled && properties.isIncludePayload();

    HttpServletRequest effectiveRequest = wrapRequest ? new CachedBodyHttpServletRequest(request) : request;
    ContentCachingResponseWrapper cachedResponse = wrapResponse ? new ContentCachingResponseWrapper(response) : null;
    HttpServletResponse effectiveResponse = wrapResponse ? cachedResponse : response;

    try {
      logRequest(effectiveRequest, requestId);
      filterChain.doFilter(effectiveRequest, effectiveResponse);
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      logResponse(cachedResponse, response.getStatus(), requestId, duration);
      if (cachedResponse != null) {
        cachedResponse.copyBodyToResponse();
      }
      MDC.remove(REQUEST_ID_ATTRIBUTE);
    }
  }

  private boolean shouldSkip(HttpServletRequest request) {
    String path = request.getRequestURI();
    return properties.getExcludedPaths().stream()
        .anyMatch(excluded -> path.startsWith(excluded) || path.contains(excluded));
  }

  private boolean shouldCacheRequestBody(HttpServletRequest request) {
    if (!properties.isIncludePayload() || !hasBody(request)) {
      return false;
    }
    // Require a known Content-Length within the cap. Chunked / unknown-length
    // requests are skipped to avoid an unbounded read into memory.
    int contentLength = request.getContentLength();
    return contentLength >= 0 && contentLength <= properties.getBodyLoggingMaxBufferBytes();
  }

  private String getOrCreateRequestId(HttpServletRequest request) {
    String requestId = request.getHeader(REQUEST_ID_HEADER);
    if (requestId == null || requestId.isBlank()) {
      requestId = UUID.randomUUID().toString();
    }
    return requestId;
  }

  private void logRequest(HttpServletRequest request, String requestId) {
    if (log.isDebugEnabled()) {
      log.debug(">>> REQUEST [{}] {} {}", requestId, request.getMethod(), request.getRequestURI());
    }
    if (!log.isTraceEnabled()) {
      return;
    }
    StringBuilder detail = new StringBuilder();
    if (properties.isIncludeQueryString() && request.getQueryString() != null) {
      String maskedQuery = masker.maskQueryString(request.getQueryString());
      detail.append(" | Query: ").append(maskedQuery);
    }
    if (properties.isIncludeHeaders()) {
      Map<String, String> headers = extractHeaders(request);
      String maskedHeaders = masker.maskHeaders(headers);
      detail.append(" | Headers: ").append(maskedHeaders);
    }
    if (request instanceof CachedBodyHttpServletRequest cached && hasBody(request)) {
      String body = cached.getCachedBody();
      if (!body.isBlank()) {
        String maskedBody = masker.maskBody(request.getContentType(), body);
        maskedBody = masker.truncate(maskedBody, properties.getMaxPayloadLength());
        detail.append(" | Body: ").append(maskedBody);
      }
    }
    if (detail.length() > 0) {
      log.trace(">>> REQUEST DETAIL [{}]{}", requestId, detail);
    }
  }

  private void logResponse(ContentCachingResponseWrapper response, int status, String requestId, long duration) {
    if (log.isDebugEnabled()) {
      log.debug("<<< RESPONSE [{}] Status: {} | Duration: {}ms", requestId, status, duration);
    }
    if (!log.isTraceEnabled() || response == null) {
      return;
    }
    byte[] content = response.getContentAsByteArray();
    if (content.length == 0) {
      return;
    }
    Charset charset = getResponseCharset(response);
    String body = new String(content, charset);
    String maskedBody = masker.maskBody(response.getContentType(), body);
    maskedBody = masker.truncate(maskedBody, properties.getMaxPayloadLength());
    log.trace("<<< RESPONSE DETAIL [{}] | Body: {}", requestId, maskedBody);
  }

  private Charset getResponseCharset(ContentCachingResponseWrapper response) {
    String encoding = response.getCharacterEncoding();
    if (encoding == null || encoding.isBlank()) {
      return StandardCharsets.UTF_8;
    }
    try {
      return Charset.forName(encoding);
    } catch (Exception e) {
      return StandardCharsets.UTF_8;
    }
  }

  private Map<String, String> extractHeaders(HttpServletRequest request) {
    Map<String, String> headers = new LinkedHashMap<>();
    Collections.list(request.getHeaderNames())
        .forEach(name -> headers.put(name, request.getHeader(name)));
    return headers;
  }

  private boolean hasBody(HttpServletRequest request) {
    String contentType = request.getContentType();
    if (contentType == null) {
      return false;
    }
    return contentType.contains(MediaType.APPLICATION_JSON_VALUE)
        || contentType.contains(MediaType.APPLICATION_XML_VALUE)
        || contentType.contains("text/")
        || contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
  }
}
