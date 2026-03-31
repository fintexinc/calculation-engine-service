package com.fintex.ce.domain.model.enumeration;

import com.fintex.ce.domain.exception.SystemException;
import com.fintex.ce.domain.exception.code.ErrorCode;

import java.util.Arrays;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public enum Currency {
  USD,
  CAD;

  private static final Map<String, Currency> MAP = Arrays.stream(values()).collect(toMap(Enum::name, a -> a));

  public static Currency get(String name) {
    return MAP.get(name);
  }

  public static Currency fromValue(final String name) {
    for (Currency value : values()) {
      if (value.name().equalsIgnoreCase(name)) {
        return value;
      }
    }
    final String message = String.format("Could not find such Currency %s", name);
    throw new SystemException(message, ErrorCode.INTERNAL_SERVER_ERROR);
  }
}
