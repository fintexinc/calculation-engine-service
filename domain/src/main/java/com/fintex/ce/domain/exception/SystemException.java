package com.fintex.ce.domain.exception;

import com.fintex.ce.domain.exception.code.HttpCode;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SystemException extends RuntimeException {

  private static final long serialVersionUID = 7153808129776996908L;

  private final HttpCode httpCode;
  private String errorEnum;
  private final HashMap<String, Object> properties = new LinkedHashMap<>();

  public SystemException(final HttpCode httpCode) {
    this.httpCode = httpCode;
  }

  public SystemException(final String message, final HttpCode httpCode) {
    super(message);
    this.httpCode = httpCode;
  }

  public SystemException(final String message, final HttpCode httpCode, final String errorEnum) {
    super(message);
    this.httpCode = httpCode;
    this.errorEnum = errorEnum;
  }

  public SystemException(final String message, final Throwable exception, final HttpCode httpCode) {
    super(message, exception);
    this.httpCode = httpCode;
  }

  public SystemException(final Throwable exception, final HttpCode httpCode) {
    super(exception);
    this.httpCode = httpCode;
  }

  public HttpCode getErrorCode() {
    return httpCode;
  }

  public String getErrorEnum() {
    return errorEnum;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public SystemException set(final String key, final Object value) {
    properties.put(key, value);
    return this;
  }

  public SystemException setProperty(Map<String, Object> properties) {
    properties.entrySet().forEach(map -> this.set(map.getKey(), map.getValue()));
    return this;
  }

  @Override
  public String getMessage() {
    final String message = super.getMessage();
    return (message == null || message.isEmpty()) ? toString() : message;
  }

  @Override
  public String toString() {
    return new StringBuilder().append("Original message: ").append(super.getMessage()).append("\n Error Code: ")
        .append(httpCode).append("\n properties: ").append(properties).toString();
  }
}
