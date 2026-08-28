package ca.tangerine.pce.application.util;

import ca.tangerine.pce.application.config.FeeProjectionProperties;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;

import static ca.tangerine.pce.application.util.DecimalUtils.divide;
import static ca.tangerine.pce.application.util.DecimalUtils.toUserScale;
import static ca.tangerine.pce.model.util.BigDecimalConstants.MATH_CONTEXT;
import static ca.tangerine.pce.model.util.BigDecimalConstants.ONE;
import static ca.tangerine.pce.model.util.BigDecimalConstants.TWELVE;

/**
 * Projects an annual fee amount forward over a {@link TimePeriod}, under the assumptions held in
 * {@link FeeProjectionProperties}.
 *
 * <p>
 * Shared by the {@code fees} metric (which projects one portfolio's spend) and {@code mer-benchmark-comparison} (which
 * projects both sides' spend to derive the saving), so the arithmetic and its rounding live in one place. Kept as a
 * utility rather than a bean: it holds no state and needs nothing from the container.
 */
@UtilityClass
public class FeeProjectionUtils {

  /**
   * Projected spend per period, in the insertion order of {@code periods}. A null {@code annualFee} — the aggregation
   * view had no defined answer — maps every period to null rather than to zero, so an absent answer is never mistaken
   * for a free portfolio.
   */
  public static Map<TimePeriod, BigDecimal> byPeriod(BigDecimal annualFee, BigDecimal growthRate,
      Collection<TimePeriod> periods) {
    Map<TimePeriod, BigDecimal> spendByPeriod = new LinkedHashMap<>();
    for (TimePeriod period : periods) {
      spendByPeriod.put(period, spend(annualFee, growthRate, period));
    }
    return spendByPeriod;
  }

  /**
   * Total fee paid over {@code period}, charged on a balance growing at {@code growthRate}.
   */
  public static BigDecimal spend(BigDecimal annualFee, BigDecimal growthRate, TimePeriod period) {
    if (annualFee == null) {
      return null;
    }
    return toUserScale(annualFee.multiply(growthFactor(growthRate, period)));
  }

  /**
   * How many "annual fees" a growing balance costs over {@code period}.
   *
   * <p>
   * The fee is charged on the balance as it stands at the start of each year, and a period that does not land on a year
   * boundary pro-rates the trailing months against the balance reached by then:
   * {@code ((1+g)^y − 1)/g + (rest/12)·(1+g)^y}, for {@code y} whole years and {@code rest} leftover months. Over whole
   * years that is the plain {@code Σ(1+g)^t}, and it degenerates to {@code months/12} when the rate is zero or absent —
   * the flat-balance case.
   *
   * <p>
   * Charging annually rather than monthly is what keeps the sub-year answers the ones a reader expects: a one-month
   * period is {@code annualFee/12}, exactly the {@code monthlyFee} the same metric already reports, and a one-year
   * period is {@code annualFee} itself at any growth rate. Accruing monthly on a monthly-compounded balance would make
   * a year cost {@code 1.027 × annualFee} at 6% and break that correspondence.
   *
   * <p>
   * Keeping the exponent a whole number of years also keeps the exact
   * {@link BigDecimal#pow(int, java.math.MathContext)} rather than routing it through
   * {@link DecimalUtils#pow(BigDecimal, BigDecimal)}, which is {@code double}-backed.
   */
  static BigDecimal growthFactor(BigDecimal growthRate, TimePeriod period) {
    int months = requireFixedLength(period);
    if (growthRate == null || growthRate.signum() == 0) {
      return divide(BigDecimal.valueOf(months), TWELVE);
    }
    int years = months / TWELVE.intValue();
    int trailingMonths = months % TWELVE.intValue();
    BigDecimal growth = ONE.add(growthRate).pow(years, MATH_CONTEXT);
    BigDecimal wholeYears = divide(growth.subtract(ONE), growthRate);
    if (trailingMonths == 0) {
      return wholeYears;
    }
    return wholeYears.add(divide(BigDecimal.valueOf(trailingMonths), TWELVE).multiply(growth));
  }

  /**
   * A projection needs a length to project over, so the length-less periods ({@code YTD}, {@code SI} and the rest) have
   * no answer here. The fee contract excludes them, so reaching this is a wiring mistake rather than bad input.
   */
  private static int requireFixedLength(TimePeriod period) {
    if (period == null || !period.isFixedLength()) {
      throw new IllegalArgumentException("Fee projection needs a period with a fixed length but got " + period);
    }
    return period.getMonths();
  }
}
