package ca.tangerine.pce.model.domain.enumeration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.experimental.UtilityClass;

import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.CIPSD;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.FIVE_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.ONE_MTH;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.SI;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.SIX_MTH;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.TEN_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.THREE_MTH;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.THREE_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.TWENTY_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.TWO_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.YTD;

import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

/**
 * Which {@link TimePeriod} values each kind of request may name.
 *
 * <p>
 * {@link TimePeriod} is the vocabulary shared with Market Investment Catalogue, so it is deliberately wider than any
 * single request: it carries every period a datapoint can be keyed by, including lengths this service has no use for
 * and length-less members only some metrics can resolve. Admissibility therefore belongs to the contract rather than to
 * the enum — a fee projection cannot answer "year to date", and a rolling window shorter than a year is not a window.
 *
 * <p>
 * Every set here is an unmodifiable {@link EnumSet}, and nothing reads it in order. Membership is the only question
 * asked of these sets: the order a report shows its periods in comes from {@code PeriodProperties} or from the request,
 * both of which keep their own insertion order, and the order the error message lists them in comes from an explicit
 * comparator in {@code AbstractSupportedPeriodsReqValidator}. Neither depends on how a set here iterates.
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
   * Horizons the fee metrics project spend over. Fixed lengths only, and deliberately the agreed reporting ladder
   * rather than every length {@link TimePeriod} happens to define: the arithmetic would handle {@code SEVEN_YR}
   * perfectly well, but nothing asks for it, and every extra column is a column the report has to explain.
   * {@link TimePeriod#ONE_MTH} is the monthly fee, {@link TimePeriod#ONE_YR} the annual one.
   */
  public static final Set<TimePeriod> FEE_PROJECTION = unmodifiable(
      List.of(ONE_MTH, THREE_MTH, SIX_MTH, ONE_YR, TWO_YR, THREE_YR, FIVE_YR, TEN_YR, TWENTY_YR));

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
