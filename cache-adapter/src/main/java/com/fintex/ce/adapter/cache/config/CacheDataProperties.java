package com.fintex.ce.adapter.cache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Binds configuration for optional cache-proxy adapters under the {@code cache.data} prefix. Each port that supports
 * caching has its own nested properties block — enabling one port's cache does not affect the others.
 */
@Data
@ConfigurationProperties(prefix = "cache.data")
public class CacheDataProperties {

  private FxRatesCacheProperties fxRates = new FxRatesCacheProperties();

  @Data
  public static class FxRatesCacheProperties {
    private boolean enabled = false;
    private int maxEntries = 4096;
    private int recencyCutoffDays = 1;
  }
}
