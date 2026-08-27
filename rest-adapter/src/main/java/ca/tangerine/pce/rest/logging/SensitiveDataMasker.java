package ca.tangerine.pce.rest.logging;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Masks sensitive data in HTTP requests before logging. Handles headers, query parameters, and JSON body content.
 *
 * <p>
 * Field, parameter and header names are matched on their letters and digits alone, ignoring case and any {@code _} or
 * {@code -} between words, so one configured {@code clientSecret} covers {@code client_secret} and
 * {@code client-secret} as well. The configuration names the <em>data</em> that must never reach a log, and that does
 * not change when the wire format spells its properties differently. A literal match keeps passing its own tests while
 * quietly masking nothing the moment a payload arrives under the other spelling.
 */
@Component
public class SensitiveDataMasker {

  /** Any JSON field and its scalar value. Which of them is sensitive is decided per match, by name. */
  private static final Pattern JSON_FIELD = Pattern.compile(
      "\"([A-Za-z0-9_-]+)\"\\s*:\\s*(\"[^\"]*\"|\\d+|true|false|null)");

  private static final Pattern NAME_SEPARATORS = Pattern.compile("[^A-Za-z0-9]");

  private final HttpLoggingProperties properties;
  private final List<Pattern> compiledPatterns;
  private final Set<String> sensitiveJsonFields;
  private final Pattern emailPattern;

  public SensitiveDataMasker(HttpLoggingProperties properties) {
    this.properties = properties;
    this.compiledPatterns = properties.getSensitivePatterns().stream()
        .map(Pattern::compile)
        .toList();
    this.sensitiveJsonFields = canonicalize(properties.getSensitiveJsonFields());
    this.emailPattern = properties.getEmailPattern() == null || properties.getEmailPattern().isBlank()
        ? null
        : Pattern.compile(properties.getEmailPattern());
  }

  /**
   * The comparable form of a field, parameter or header name: letters and digits, lower case. Drops the separators that
   * distinguish {@code clientSecret} from {@code client_secret} without distinguishing the data they carry.
   */
  private static String canonicalName(String name) {
    return NAME_SEPARATORS.matcher(name).replaceAll("").toLowerCase(Locale.ROOT);
  }

  private static Set<String> canonicalize(Set<String> names) {
    return names == null
        ? Set.of()
        : names.stream().map(SensitiveDataMasker::canonicalName).collect(Collectors.toUnmodifiableSet());
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
    String masked = sensitiveJsonFields.isEmpty() ? body : maskJsonFields(body);
    return applyPatternMasking(masked);
  }

  private String maskJsonFields(String json) {
    Matcher matcher = JSON_FIELD.matcher(json);
    StringBuilder result = new StringBuilder();
    while (matcher.find()) {
      String fieldName = matcher.group(1);
      if (sensitiveJsonFields.contains(canonicalName(fieldName))) {
        String replacement = "\"" + fieldName + "\": \"" + properties.getMaskValue() + "\"";
        matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
      }
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
    return matchesAnyName(properties.getSensitiveHeaders(), headerName);
  }

  private boolean isSensitiveParameter(String paramName) {
    return matchesAnyName(properties.getSensitiveParameters(), paramName);
  }

  private static boolean matchesAnyName(Set<String> configured, String name) {
    if (configured == null || name == null) {
      return false;
    }
    String canonical = canonicalName(name);
    return configured.stream().anyMatch(sensitive -> canonicalName(sensitive).equals(canonical));
  }

  public String truncate(String content, int maxLength) {
    if (content == null || content.length() <= maxLength) {
      return content;
    }
    return content.substring(0, maxLength) + "...[TRUNCATED]";
  }
}
