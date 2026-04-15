package com.fintex.ce.adapter.rest.logging;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SensitiveDataMasker {

  private final HttpLoggingProperties properties;
  private final List<Pattern> compiledPatterns;
  private final Pattern jsonFieldPattern;
  private final Pattern emailPattern;

  public SensitiveDataMasker(HttpLoggingProperties properties) {
    this.properties = properties;
    this.compiledPatterns = properties.getSensitivePatterns().stream()
        .map(Pattern::compile)
        .toList();
    this.jsonFieldPattern = buildJsonFieldPattern(properties.getSensitiveJsonFields());
    this.emailPattern = properties.getEmailPattern() == null || properties.getEmailPattern().isBlank()
        ? null
        : Pattern.compile(properties.getEmailPattern());
  }

  private Pattern buildJsonFieldPattern(Set<String> fields) {
    if (fields == null || fields.isEmpty()) {
      return null;
    }
    String fieldGroup = fields.stream()
        .map(Pattern::quote)
        .collect(Collectors.joining("|"));
    String regex = "\"(" + fieldGroup + ")\"\\s*:\\s*(\"[^\"]*\"|\\d+|true|false|null)";
    return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
  }

  public String maskHeaders(Map<String, String> headers) {
    if (headers == null || headers.isEmpty()) {
      return "{}";
    }
    return headers.entrySet().stream()
        .map(entry -> {
          String key = entry.getKey();
          String value = isSensitiveHeader(key) ? properties.getMaskValue() : entry.getValue();
          return key + ": " + value;
        })
        .collect(Collectors.joining(", ", "{", "}"));
  }

  public String maskQueryString(String queryString) {
    if (queryString == null || queryString.isEmpty()) {
      return queryString;
    }
    StringBuilder result = new StringBuilder();
    String[] pairs = queryString.split("&");
    for (int i = 0; i < pairs.length; i++) {
      if (i > 0) {
        result.append("&");
      }
      String[] keyValue = pairs[i].split("=", 2);
      String key = keyValue[0];
      if (keyValue.length > 1) {
        String value = isSensitiveParameter(key) ? properties.getMaskValue() : keyValue[1];
        result.append(key).append("=").append(value);
      } else {
        result.append(key);
      }
    }
    return result.toString();
  }

  /**
   * Routes the body through the appropriate masker for the given content type. Form-encoded bodies are masked as query
   * strings (so {@code password=secret} is masked); everything else is treated as JSON.
   */
  public String maskBody(String contentType, String body) {
    if (body == null || body.isEmpty()) {
      return body;
    }
    if (contentType != null && contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
      String masked = maskQueryString(body);
      return applyPatternMasking(masked);
    }
    return maskJsonBody(body);
  }

  public String maskJsonBody(String body) {
    if (body == null || body.isEmpty()) {
      return body;
    }
    String masked = body;
    if (jsonFieldPattern != null) {
      masked = maskJsonFields(masked);
    }
    masked = applyPatternMasking(masked);
    return masked;
  }

  private String maskJsonFields(String json) {
    if (jsonFieldPattern == null) {
      return json;
    }
    Matcher matcher = jsonFieldPattern.matcher(json);
    StringBuilder result = new StringBuilder();
    while (matcher.find()) {
      String fieldName = matcher.group(1);
      String replacement = "\"" + fieldName + "\": \"" + properties.getMaskValue() + "\"";
      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(result);
    return result.toString();
  }

  private String applyPatternMasking(String text) {
    String result = text;
    for (Pattern pattern : compiledPatterns) {
      result = pattern.matcher(result).replaceAll(properties.getMaskValue());
    }
    if (emailPattern != null && properties.isMaskEmails()) {
      result = emailPattern.matcher(result).replaceAll(properties.getMaskValue());
    }
    return result;
  }

  private boolean isSensitiveHeader(String headerName) {
    return properties.getSensitiveHeaders().stream()
        .anyMatch(sensitive -> sensitive.equalsIgnoreCase(headerName));
  }

  private boolean isSensitiveParameter(String paramName) {
    return properties.getSensitiveParameters().stream()
        .anyMatch(sensitive -> sensitive.equalsIgnoreCase(paramName));
  }

  public String truncate(String content, int maxLength) {
    if (content == null || content.length() <= maxLength) {
      return content;
    }
    return content.substring(0, maxLength) + "...[TRUNCATED]";
  }
}
