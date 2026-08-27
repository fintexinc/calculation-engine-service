package ca.tangerine.pce.cache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.Data;

/**
 * Binds configuration for optional cache-proxy adapters under the {@code cache.data} prefix. Each port that supports
 * caching has its own nested properties block — enabling one port's cache does not affect the others.
 */
@Data
@ConfigurationProperties(prefix = "cache.data")
public class CacheDataProperties {

  private FxRatesCacheProperties fxRates = new FxRatesCacheProperties();
  private TBillsCacheProperties tBills = new TBillsCacheProperties();

  @Data
  public static class FxRatesCacheProperties {
    private boolean enabled = false;
    private int maxEntries = 4096;
  }

  @Data
  public static class TBillsCacheProperties {
    private boolean enabled = false;
    /**
     * TTL for each cached per-currency series. Accepts Spring's {@link Duration} formats — {@code 30m}, {@code 1h},
     * {@code PT1H}. The {@link DurationUnit} fallback applies when the value is supplied as a bare integer.
     */
    @DurationUnit(ChronoUnit.HOURS)
    private Duration refreshAfter = Duration.ofHours(24);
  }
}
