package com.fintex.ce.adapter.rest.logging;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.fintex.ce.adapter.rest.util.HttpConstants.REQUEST_ID_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

class RequestLoggingFilterTest {

  private final HttpLoggingProperties properties = new HttpLoggingProperties();
  private final SensitiveDataMasker masker = new SensitiveDataMasker(properties);
  private final RequestLoggingFilter filter = new RequestLoggingFilter(properties, masker);
  private final Logger filterLogger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);

  {
    filterLogger.setLevel(Level.TRACE);
  }

  @Test
  void skipsExcludedPaths() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filter.doFilterInternal(request, response, filterChain);

    assertNotNull(filterChain.getRequest());
  }

  @Test
  void setsRequestIdHeader_whenNotPresent() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/portfolio/calculations/test");
    request.setContentType(MediaType.APPLICATION_JSON_VALUE);
    request.setContent("{}".getBytes(StandardCharsets.UTF_8));
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filter.doFilterInternal(request, response, filterChain);

    String header = response.getHeader(REQUEST_ID_HEADER);
    assertNotNull(header);
    // Full UUID, not the previous 8-char prefix.
    assertEquals(UUID.fromString(header).toString(), header);
  }

  @Test
  void preservesExistingRequestId() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/portfolio/calculations/test");
    request.addHeader(REQUEST_ID_HEADER, "existing-id");
    request.setContentType(MediaType.APPLICATION_JSON_VALUE);
    request.setContent("{}".getBytes(StandardCharsets.UTF_8));
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filter.doFilterInternal(request, response, filterChain);

    assertEquals("existing-id", response.getHeader(REQUEST_ID_HEADER));
  }

  @Test
  void preservesResponseBody() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/portfolio/calculations/test");
    request.setContentType(MediaType.APPLICATION_JSON_VALUE);
    request.setContent("{\"data\": \"test\"}".getBytes(StandardCharsets.UTF_8));
    MockHttpServletResponse response = new MockHttpServletResponse();
    Filter responseWriter = (req, res, chain) -> {
      res.getOutputStream().write("{\"result\": \"ok\"}".getBytes(StandardCharsets.UTF_8));
      res.setContentType(MediaType.APPLICATION_JSON_VALUE);
    };
    MockFilterChain filterChain = new MockFilterChain(new HttpServlet() {}, responseWriter);

    filter.doFilterInternal(request, response, filterChain);

    assertEquals("{\"result\": \"ok\"}", response.getContentAsString());
  }

  @Test
  void chainsRequestToNextFilter() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filter.doFilterInternal(request, response, filterChain);

    assertNotNull(filterChain.getRequest());
  }

  @Test
  void putsRequestIdInMdcDuringChain_andRemovesAfterwards() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<String> mdcValueDuringChain = new AtomicReference<>();
    Filter probe = (req, res, chain) -> mdcValueDuringChain.set(MDC.get("requestId"));
    MockFilterChain filterChain = new MockFilterChain(new HttpServlet() {}, probe);

    filter.doFilterInternal(request, response, filterChain);

    assertNotNull(mdcValueDuringChain.get());
    assertEquals(response.getHeader(REQUEST_ID_HEADER), mdcValueDuringChain.get());
    assertNull(MDC.get("requestId"));
  }

  @Test
  void doesNotWrapRequest_whenPayloadLoggingDisabled() throws ServletException, IOException {
    properties.setIncludePayload(false);
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/test");
    request.setContentType(MediaType.APPLICATION_JSON_VALUE);
    request.setContent("{}".getBytes(StandardCharsets.UTF_8));
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<ServletRequest> seen = new AtomicReference<>();
    Filter probe = (req, res, chain) -> seen.set(req);
    MockFilterChain filterChain = new MockFilterChain(new HttpServlet() {}, probe);

    filter.doFilterInternal(request, response, filterChain);

    assertSame(request, seen.get());
    assertFalse(seen.get() instanceof CachedBodyHttpServletRequest);
  }

  @Test
  void wrapsRequest_whenPayloadLoggingEnabled() throws ServletException, IOException {
    properties.setIncludePayload(true);
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/test");
    request.setContentType(MediaType.APPLICATION_JSON_VALUE);
    request.setContent("{\"a\":1}".getBytes(StandardCharsets.UTF_8));
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<ServletRequest> seen = new AtomicReference<>();
    Filter probe = (req, res, chain) -> seen.set(req);
    MockFilterChain filterChain = new MockFilterChain(new HttpServlet() {}, probe);

    filter.doFilterInternal(request, response, filterChain);

    assertInstanceOf(CachedBodyHttpServletRequest.class, seen.get());
  }

  @Test
  void doesNotWrapRequest_whenBodyExceedsBufferCap() throws ServletException, IOException {
    properties.setIncludePayload(true);
    properties.setBodyLoggingMaxBufferBytes(8);
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/test");
    request.setContentType(MediaType.APPLICATION_JSON_VALUE);
    byte[] big = "{\"x\":\"123456789\"}".getBytes(StandardCharsets.UTF_8);
    request.setContent(big);
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<ServletRequest> seen = new AtomicReference<>();
    Filter probe = (req, res, chain) -> seen.set(req);
    MockFilterChain filterChain = new MockFilterChain(new HttpServlet() {}, probe);

    filter.doFilterInternal(request, response, filterChain);

    assertSame(request, seen.get());
  }

  @Test
  void doesNotWrapRequest_whenContentLengthUnknown() throws ServletException, IOException {
    properties.setIncludePayload(true);
    // Simulate chunked / unknown-length request: setContent with null leaves content-length at -1.
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/test") {
      @Override
      public int getContentLength() {
        return -1;
      }
    };
    request.setContentType(MediaType.APPLICATION_JSON_VALUE);
    request.setContent("{\"a\":1}".getBytes(StandardCharsets.UTF_8));
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<ServletRequest> seen = new AtomicReference<>();
    Filter probe = (req, res, chain) -> seen.set(req);
    MockFilterChain filterChain = new MockFilterChain(new HttpServlet() {}, probe);

    filter.doFilterInternal(request, response, filterChain);

    assertSame(request, seen.get());
    assertFalse(seen.get() instanceof CachedBodyHttpServletRequest);
  }

  @Test
  void doesNotWrapRequest_whenTraceLevelDisabled() throws ServletException, IOException {
    properties.setIncludePayload(true);
    filterLogger.setLevel(Level.DEBUG); // bodies log at TRACE — at DEBUG, no need to buffer
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/test");
    request.setContentType(MediaType.APPLICATION_JSON_VALUE);
    request.setContent("{\"a\":1}".getBytes(StandardCharsets.UTF_8));
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<ServletRequest> seen = new AtomicReference<>();
    Filter probe = (req, res, chain) -> seen.set(req);
    MockFilterChain filterChain = new MockFilterChain(new HttpServlet() {}, probe);

    filter.doFilterInternal(request, response, filterChain);

    assertSame(request, seen.get());
    assertFalse(seen.get() instanceof CachedBodyHttpServletRequest);
  }

  @Test
  void downstreamCanReadFullBody_whenWrapped() throws ServletException, IOException {
    properties.setIncludePayload(true);
    String payload = "{\"hello\":\"world\"}";
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/test");
    request.setContentType(MediaType.APPLICATION_JSON_VALUE);
    request.setContent(payload.getBytes(StandardCharsets.UTF_8));
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<String> seenBody = new AtomicReference<>();
    Filter probe = (req, res, chain) -> {
      HttpServletRequest http = (HttpServletRequest) req;
      seenBody.set(new String(http.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
    };
    MockFilterChain filterChain = new MockFilterChain(new HttpServlet() {}, probe);

    filter.doFilterInternal(request, response, filterChain);

    assertEquals(payload, seenBody.get());
  }
}
