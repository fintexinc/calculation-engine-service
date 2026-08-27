package ca.tangerine.pce.application.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.TEN_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.TWENTY_YR;

import ca.tangerine.pce.application.util.FeeProjectionUtils;
import ca.tangerine.pce.model.domain.enumeration.SupportedPeriods;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

/**
 * Assumptions behind the projected fee figures reported by the {@code fees} and {@code mer-benchmark-comparison}
 * metrics.
 *
 * <p>
 * The projection charges the fee on the balance as it stands at the start of each year and sums it, with the balance
 * growing at {@link #annualGrowthRate}. The fee is <b>not</b> compounded as a drag — the balance grows at {@code g}
 * regardless of what is paid out in fees. See {@link FeeProjectionUtils#growthFactor} for the closed form and for what
 * happens to a period that does not land on a year boundary.
 *
 * <p>
 * Both values are configuration rather than constants because they move the long-horizon answers materially. A 2% fee
 * on $100k — an annual fee of $2,000 — projects to $40,000 over twenty years on a flat balance and $73,571.18 once the
 * balance grows at 6%: the assumption nearly doubles the answer, so callers cannot read a 20Y figure without knowing
 * which one produced it.
 */
@Getter
@Setter
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "calculation.fee.projection")
public class FeeProjectionProperties implements InitializingBean {

  /**
   * {@code -1} is the balance losing everything in year one: degenerate but sound, since the projection then collapses
   * to a single annual fee. Below it the balance would lose more than 100%, which no portfolio can do, and
   * {@code (1 + g)} turns negative — its powers then alternate sign, so the reported spend swings between years instead
   * of accumulating.
   */
  static final BigDecimal MIN_GROWTH_RATE = new BigDecimal("-1");

  /**
   * Periods the projection reports. {@link TimePeriod#ONE_MTH} reproduces the monthly fee and {@link TimePeriod#ONE_YR}
   * the annual one, whatever the growth rate.
   *
   * <p>
   * Seeded from a {@link List} rather than a {@code Set.of(...)}: iteration order is the order the columns are reported
   * in, and {@code Set.of} does not specify one.
   */
  private Set<TimePeriod> periods = new LinkedHashSet<>(List.of(ONE_YR, TEN_YR, TWENTY_YR));

  /**
   * Assumed annual growth of the portfolio balance the fee is charged on, as a ratio ({@code 0.06} = 6%). Zero means a
   * flat balance.
   */
  private BigDecimal annualGrowthRate = new BigDecimal("0.06");

  /**
   * Fails the context at startup rather than per request. Both values are only reachable through
   * {@code application.yml} or an environment override, so a bad one is a deployment mistake: catching it here turns it
   * into a boot failure the deployer sees, instead of a 500 that surfaces later on whichever request first hits the
   * projection. Binding runs in a bean post-processor, which is ahead of this callback, so the bound values are what
   * get checked. Per-request periods are validated separately at the REST boundary, where a bad value is the caller's
   * mistake and belongs in a 400.
   */
  @Override
  public void afterPropertiesSet() {
    validateAssumptions();
  }

  void validateAssumptions() {
    if (CollectionUtils.isEmpty(periods)) {
      throw new IllegalStateException(
          "calculation.fee.projection.periods must list at least one period, but was " + periods);
    }
    periods.stream()
        .filter(period -> !SupportedPeriods.FEE_PROJECTION.contains(period))
        .findFirst()
        .ifPresent(period -> {
          throw new IllegalStateException("calculation.fee.projection.periods names " + period
              + ", which the fee metrics cannot project. Admissible periods are: " + SupportedPeriods.FEE_PROJECTION);
        });
    if (annualGrowthRate == null || annualGrowthRate.compareTo(MIN_GROWTH_RATE) < 0) {
      throw new IllegalStateException("calculation.fee.projection.annual-growth-rate must be " + MIN_GROWTH_RATE
          + " or greater, but was " + annualGrowthRate);
    }
  }

  /**
   * The periods to report for one request: what the caller asked for, or these defaults when they asked for nothing. A
   * caller-supplied set is taken as sent, in its own order — the report screens differ in how many columns they show,
   * so the server default is a fallback rather than a ceiling. Values are validated at the request boundary, so an
   * inadmissible period is a 400 rather than a failure in the middle of a calculation.
   */
  public Set<TimePeriod> periodsFor(Set<TimePeriod> requested) {
    return CollectionUtils.isEmpty(requested) ? periods : requested;
  }
}
