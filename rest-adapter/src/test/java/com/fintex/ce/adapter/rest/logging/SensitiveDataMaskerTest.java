package com.fintex.ce.adapter.rest.logging;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensitiveDataMaskerTest {

  private final HttpLoggingProperties properties = new HttpLoggingProperties();
  private final SensitiveDataMasker masker = new SensitiveDataMasker(properties);

  @Test
  void maskHeaders_masksSensitiveHeaders() {
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    headers.put(HttpHeaders.AUTHORIZATION, "Bearer secret-token");
    headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

    String result = masker.maskHeaders(headers);

    assertEquals("{Content-Type: application/json, Authorization: [MASKED], Accept: application/json}", result);
  }

  @Test
  void maskHeaders_returnsEmptyBraces_whenNull() {
    assertEquals("{}", masker.maskHeaders(null));
  }

  @Test
  void maskHeaders_returnsEmptyBraces_whenEmpty() {
    assertEquals("{}", masker.maskHeaders(Map.of()));
  }

  @Test
  void maskQueryString_masksSensitiveParams() {
    String result = masker.maskQueryString("name=John&password=secret123&page=1");

    assertEquals("name=John&password=[MASKED]&page=1", result);
  }

  @Test
  void maskQueryString_preservesNonSensitiveParams() {
    String result = masker.maskQueryString("name=John&page=1");

    assertEquals("name=John&page=1", result);
  }

  @Test
  void maskQueryString_returnsNull_whenNull() {
    assertNull(masker.maskQueryString(null));
  }

  @Test
  void maskQueryString_returnsEmpty_whenEmpty() {
    assertEquals("", masker.maskQueryString(""));
  }

  @Test
  void maskQueryString_handlesParamWithoutValue() {
    String result = masker.maskQueryString("name=John&flag&page=1");

    assertEquals("name=John&flag&page=1", result);
  }

  @Test
  void maskJsonBody_masksSensitiveFields() {
    String json = """
        {"username": "john", "password": "secret123", "email": "john@example.com"}""";

    String result = masker.maskJsonBody(json);

    assertEquals("""
        {"username": "john", "password": "[MASKED]", "email": "john@example.com"}""", result);
  }

  @Test
  void maskJsonBody_masksMultipleSensitiveFields() {
    String json = """
        {"accessToken": "abc123", "refreshToken": "def456"}""";

    String result = masker.maskJsonBody(json);

    assertEquals("""
        {"accessToken": "[MASKED]", "refreshToken": "[MASKED]"}""", result);
  }

  @Test
  void maskJsonBody_masksNumericValues() {
    String json = """
        {"pin": 1234, "name": "John"}""";

    String result = masker.maskJsonBody(json);

    assertEquals("""
        {"pin": "[MASKED]", "name": "John"}""", result);
  }

  @Test
  void maskJsonBody_returnsNull_whenNull() {
    assertNull(masker.maskJsonBody(null));
  }

  @Test
  void maskJsonBody_returnsEmpty_whenEmpty() {
    assertEquals("", masker.maskJsonBody(""));
  }

  @Test
  void truncate_truncatesLongContent() {
    String longContent = "a".repeat(200);

    String result = masker.truncate(longContent, 100);

    assertEquals(100 + "...[TRUNCATED]".length(), result.length());
    assertEquals("a".repeat(100) + "...[TRUNCATED]", result);
  }

  @Test
  void truncate_returnsOriginal_whenWithinLimit() {
    String content = "short content";

    String result = masker.truncate(content, 100);

    assertEquals("short content", result);
  }

  @Test
  void truncate_returnsNull_whenNull() {
    assertNull(masker.truncate(null, 100));
  }

  @Test
  void maskJsonBody_doesNotMaskEmail_byDefault() {
    String result = masker.maskJsonBody("""
        {"contact": "john@example.com"}""");

    assertTrue(result.contains("john@example.com"), "email should not be masked when maskEmails is false");
  }

  @Test
  void maskJsonBody_masksEmail_whenEnabled() {
    properties.setMaskEmails(true);

    String result = masker.maskJsonBody("""
        {"contact": "john@example.com"}""");

    assertEquals("""
        {"contact": "[MASKED]"}""", result);
  }

  @Test
  void maskBody_routesFormEncodedThroughQueryStringMasker() {
    String body = "username=john&password=secret123&page=1";

    String result = masker.maskBody(MediaType.APPLICATION_FORM_URLENCODED_VALUE, body);

    assertEquals("username=john&password=[MASKED]&page=1", result);
  }

  @Test
  void maskBody_routesJsonThroughJsonMasker() {
    String body = """
        {"password": "secret"}""";

    String result = masker.maskBody(MediaType.APPLICATION_JSON_VALUE, body);

    assertEquals("""
        {"password": "[MASKED]"}""", result);
  }
}
