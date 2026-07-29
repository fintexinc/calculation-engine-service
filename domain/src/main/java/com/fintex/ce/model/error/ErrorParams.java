package com.fintex.ce.model.error;

import com.fintex.ce.model.domain.holding.PortfolioHolding;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;

/**
 * Shared helpers for assembling error/notification context. Centralises the param-1, param-2, ... metadata convention
 * and the holding-identifier extraction so {@link ErrorCode} and
 * {@link com.fintex.ce.model.error.exceptions.BasePceException} agree on how parameters and holding context flow into a
 * {@link com.fintex.wm.commons.error.Notification}.
 */
@UtilityClass
public final class ErrorParams {

  public static final String PARAM_KEY_PREFIX = "param-";

  /**
   * Metadata key under which {@link #holdingId(PortfolioHolding)} is stored on a {@link Notification} or
   * {@link com.fintex.ce.model.error.exceptions.BasePceException}. This metadata entry is the single source of truth
   * for the holding identifier: {@link com.fintex.wm.commons.error.Notification#getUuid()} always carries a freshly
   * generated UUID, never the holding id.
   */
  public static final String HOLDING_ID = "holdingId";

  /**
   * Returns the holding's identifier string. Stored in metadata under {@link #HOLDING_ID} and, for exceptions, also in
   * {@link com.fintex.ce.model.error.exceptions.BasePceException#getId()}.
   */
  public static String holdingId(PortfolioHolding holding) {
    return holding == null ? null : holding.getIdsString();
  }

  /**
   * Prepends a value to the front of a varargs array, preserving order of the rest.
   */
  public static Object[] prepend(Object first, Object[] rest) {
    if (rest == null || rest.length == 0) {
      return new Object[] {first};
    }
    Object[] all = new Object[rest.length + 1];
    all[0] = first;
    System.arraycopy(rest, 0, all, 1, rest.length);
    return all;
  }

  /**
   * Builds a fresh metadata map from a positional arg array, keyed {@code param-1, param-2, ...}.
   */
  public static Map<String, Object> paramMetadata(Object[] formatArgs) {
    Map<String, Object> metadata = new LinkedHashMap<>();
    putParams(metadata, formatArgs);
    return metadata;
  }

  /**
   * Inserts positional args into the supplied metadata map under {@code param-1, param-2, ...} keys.
   */
  public static void putParams(Map<String, Object> metadata, Object[] formatArgs) {
    if (formatArgs == null) {
      return;
    }
    for (int i = 0; i < formatArgs.length; i++) {
      metadata.put(PARAM_KEY_PREFIX + (i + 1), formatArgs[i]);
    }
  }
}
