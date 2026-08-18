package com.fintex.ce.application.config;

import com.fintex.ce.model.domain.enumeration.SupportedPeriods;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The default period sets each metric family reports when a request names none. One typed bean in place of the
 * {@code @Value("#{'${default.periods.…}'.split(',')}")} expression that used to be repeated at two dozen constructors
 * — the same key was spelled out sixteen times for the risk metrics alone, and one site bound it without the split at
 * all, which no reader could have told apart from the others.
 *
 * <p>
 * Typing the sets as {@link TimePeriod} does more than tidy the constructors. An unknown value can no longer reach a
 * calculation: it fails to bind and the context refuses to start, naming the property. It also retires the trimming
 * that used to be needed throughout the returns pipeline, where {@code "12, 36, 60"} arrived as {@code " 36"} and every
 * consumer had to remember to strip it.
 */
@Getter
@Setter
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "default.periods")
public class PeriodProperties implements InitializingBean {

  private Set<TimePeriod> riskCalculations = new LinkedHashSet<>();

  private Set<TimePeriod> trailingTotalReturns = new LinkedHashSet<>();

  /**
   * Refuses to start on a default set that is empty or that names a period the metric family cannot answer.
   *
   * <p>
   * Both checks earn their place. The SpEL expressions failed placeholder resolution when a key was missing from
   * {@code application.yml}, and losing that would let a metric silently report no periods at all. And a default set is
   * the one place an inadmissible period would never be caught by request validation — configuring
   * {@code risk-calculations: 6} would have every risk metric answer null for a period no caller ever asked for.
   */
  @Override
  public void afterPropertiesSet() {
    byKey().forEach(PeriodProperties::validate);
  }

  private static void validate(final String key, final ConfiguredSet configured) {
    if (CollectionUtils.isEmpty(configured.periods())) {
      throw new IllegalStateException("default.periods." + key + " must list at least one period");
    }
    configured.periods().stream()
        .filter(period -> !configured.admissible().contains(period))
        .findFirst()
        .ifPresent(period -> {
          throw new IllegalStateException("default.periods." + key + " names " + period
              + ", which this metric family cannot report. Admissible periods are: " + configured.admissible());
        });
  }

  private Map<String, ConfiguredSet> byKey() {
    Map<String, ConfiguredSet> byKey = new LinkedHashMap<>();
    byKey.put("risk-calculations", new ConfiguredSet(riskCalculations, SupportedPeriods.TWELVE_MONTH_MINIMUM));
    byKey.put("trailing-total-returns", new ConfiguredSet(trailingTotalReturns, SupportedPeriods.TRAILING_RETURNS));
    return byKey;
  }

  private record ConfiguredSet(Set<TimePeriod> periods, Set<TimePeriod> admissible) {
  }
}
