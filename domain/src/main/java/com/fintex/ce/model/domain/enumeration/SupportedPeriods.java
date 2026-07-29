package com.fintex.ce.model.domain.enumeration;

import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.experimental.UtilityClass;

import static com.fintex.wm.commons.domain.enumeration.TimePeriod.CIPSD;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.SI;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.YTD;

/**
 * Which {@link TimePeriod} values each kind of request may name.
 *
 * <p>
 * {@link TimePeriod} is the vocabulary shared with Security Master, so it is deliberately wider than any single
 * request: it carries every period a datapoint can be keyed by, including lengths this service has no use for and
 * length-less members only some metrics can resolve. Admissibility therefore belongs to the contract rather than to the
 * enum — a fee projection cannot answer "year to date", and a rolling window shorter than a year is not a window.
 *
 * <p>
 * These sets replace what used to be a class per rule — {@code PeriodLessThan12ReqValidator},
 * {@code RollingPeriodsLessThan12ReqValidator} and three {@code PeriodsNotContaining*} validators, each re-deriving its
 * own predicate over an untyped string. One declared set per contract says the same thing in one readable place, and
 * puts the allowed values within reach of the error message.
 */
@UtilityClass
public class SupportedPeriods {

  /** Every period that has a length of its own, i.e. everything except the data- and request-resolved members. */
  public static final Set<TimePeriod> FIXED_LENGTH = unmodifiable(
      Arrays.stream(TimePeriod.values()).filter(TimePeriod::isFixedLength).toList());

  /**
   * Trailing returns, which can look back over a fixed window or over one the data defines. The widest set in the
   * service, and the only one admitting {@link TimePeriod#CIPSD}, whose length comes from a date on the request.
   */
  public static final Set<TimePeriod> TRAILING_RETURNS = with(FIXED_LENGTH, YTD, SI, CIPSD);

  /**
   * Risk and rolling metrics, whose statistics need at least twelve monthly observations to mean anything. Sub-year
   * lengths are refused outright rather than accepted and answered with null.
   */
  public static final Set<TimePeriod> TWELVE_MONTH_MINIMUM = unmodifiable(
      FIXED_LENGTH.stream().filter(period -> period.getMonths() >= ONE_YR.getMonths()).toList());

  private static Set<TimePeriod> unmodifiable(final Collection<TimePeriod> periods) {
    return Collections.unmodifiableSet(EnumSet.copyOf(periods));
  }

  private static Set<TimePeriod> with(final Set<TimePeriod> base, final TimePeriod... extra) {
    EnumSet<TimePeriod> combined = EnumSet.copyOf(base);
    combined.addAll(List.of(extra));
    return Collections.unmodifiableSet(combined);
  }
}
