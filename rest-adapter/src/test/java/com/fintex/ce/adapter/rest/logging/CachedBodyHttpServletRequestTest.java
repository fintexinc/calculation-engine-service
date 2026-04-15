package com.fintex.ce.adapter.rest.logging;

import org.springframework.mock.web.MockHttpServletRequest;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CachedBodyHttpServletRequestTest {

  @Test
  void getCachedBody_returnsSameContent() throws IOException {
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.setContent("{\"key\": \"value\"}".getBytes(StandardCharsets.UTF_8));

    CachedBodyHttpServletRequest cached = new CachedBodyHttpServletRequest(mockRequest);

    assertEquals("{\"key\": \"value\"}", cached.getCachedBody());
  }

  @Test
  void getInputStream_canBeReadMultipleTimes() throws IOException {
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.setContent("test body".getBytes(StandardCharsets.UTF_8));

    CachedBodyHttpServletRequest cached = new CachedBodyHttpServletRequest(mockRequest);

    String firstRead = new String(cached.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    String secondRead = new String(cached.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

    assertEquals("test body", firstRead);
    assertEquals("test body", secondRead);
  }

  @Test
  void getReader_canBeReadMultipleTimes() throws IOException {
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.setContent("test body".getBytes(StandardCharsets.UTF_8));

    CachedBodyHttpServletRequest cached = new CachedBodyHttpServletRequest(mockRequest);

    String firstRead = readFully(cached.getReader());
    String secondRead = readFully(cached.getReader());

    assertEquals("test body", firstRead);
    assertEquals("test body", secondRead);
  }

  @Test
  void handlesEmptyBody() throws IOException {
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.setContent(new byte[0]);

    CachedBodyHttpServletRequest cached = new CachedBodyHttpServletRequest(mockRequest);

    assertEquals("", cached.getCachedBody());
  }

  @Test
  void respectsRequestCharacterEncoding() throws IOException {
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.setCharacterEncoding("ISO-8859-1");
    // 'café' contains a non-ASCII char that differs between ISO-8859-1 and UTF-8.
    String original = "caf\u00E9";
    mockRequest.setContent(original.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));

    CachedBodyHttpServletRequest cached = new CachedBodyHttpServletRequest(mockRequest);

    assertEquals(original, cached.getCachedBody());
    assertEquals(original, readFully(cached.getReader()));
  }

  @Test
  void fallsBackToUtf8_whenNoCharacterEncoding() throws IOException {
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.setCharacterEncoding(null);
    mockRequest.setContent("hello".getBytes(StandardCharsets.UTF_8));

    CachedBodyHttpServletRequest cached = new CachedBodyHttpServletRequest(mockRequest);

    assertEquals("hello", cached.getCachedBody());
  }

  private String readFully(BufferedReader reader) throws IOException {
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      sb.append(line);
    }
    return sb.toString();
  }
}
