package com.fintex.ce.util;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.constant.CacheNameEntity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CacheUtils {

  private CacheUtils() {
  }

  public static String buildCacheName(final CacheNameEntity prefix, final CacheCategory category) {
    return Objects.requireNonNull(prefix) + "_" + Objects.requireNonNull(category);
  }

  /**
   * Builds id for list of data providers
   *
   * @param providers
   *          data providers
   * @return a string as (Enum.ordinal()): 0_1
   */
  public static String buildIdBasedOnProviders(final List<DataProvider> providers) {
    return Objects.requireNonNull(providers).stream()
        .map(e -> e.ordinal() + "")
        .collect(Collectors.joining("_", "", ""));
  }

}
