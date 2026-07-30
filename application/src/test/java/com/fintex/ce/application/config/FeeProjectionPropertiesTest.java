package com.fintex.ce.application.config;

import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_MTH;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.TEN_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.THREE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.TWENTY_YR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Typing the periods removed two of the cases this used to cover — a horizon below one year, and a null one — because
 * neither is representable any more. What replaces them is the case the old check could not express: a period that is
 * perfectly real but not one the fee metrics project over.
 */
class FeeProjectionPropertiesTest {

  @Test
  void shouldAcceptTheShippedDefaults() {
    var properties = new FeeProjectionProperties();

    assertThatCode(properties::validateAssumptions).doesNotThrowAnyException();
    assertThat(properties.getPeriods()).containsExactly(ONE_YR, TEN_YR, TWENTY_YR);
    assertThat(properties.getAnnualGrowthRate()).isEqualByComparingTo("0.06");
  }

  @Test
  void shouldRejectAnEmptyPeriodSet_whichWouldReportNoProjectionAtAll() {
    var properties = propertiesWith(Set.of(), "0.06");

    assertThatThrownBy(properties::validateAssumptions)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("calculation.fee.projection.periods")
        .hasMessageContaining("at least one period");
  }

  /** Length-less periods have nothing to project over, so configuring one would fail mid-calculation. */
  @ParameterizedTest
  @EnumSource(value = TimePeriod.class, names = {"YTD", "SI", "QTD", "OVERALL", "ITD", "CIPSD"})
  void shouldRejectAPeriodWithNoFixedLength(TimePeriod period) {
    var properties = propertiesWith(Set.of(ONE_YR, period), "0.06");

    assertThatThrownBy(properties::validateAssumptions)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("calculation.fee.projection.periods")
        .hasMessageContaining(period.name());
  }

  /**
   * {@code SEVEN_YR} has a length and the arithmetic would handle it, but it is not on the reporting ladder — the fee
   * contract narrows the shared vocabulary, and configuration is held to the same set a request is.
   */
  @ParameterizedTest
  @EnumSource(value = TimePeriod.class, names = {"SEVEN_YR", "FOUR_MTH", "THIRTY_YR"})
  void shouldRejectAFixedLengthPeriodOutsideTheReportingLadder(TimePeriod period) {
    var properties = propertiesWith(Set.of(ONE_YR, period), "0.06");

    assertThatThrownBy(properties::validateAssumptions)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(period.name());
  }

  @ParameterizedTest
  @ValueSource(strings = {"-1.01", "-2"})
  void shouldRejectAGrowthRateBelowMinusOne_whichWouldMakeTheProjectionOscillate(String growthRate) {
    // (1 + g) turns negative, so its powers alternate sign and the spend swings instead of growing
    var properties = propertiesWith(Set.of(ONE_YR, TEN_YR, TWENTY_YR), growthRate);

    assertThatThrownBy(properties::validateAssumptions)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("calculation.fee.projection.annual-growth-rate")
        .hasMessageContaining("-1 or greater");
  }

  @Test
  void shouldRejectAnAbsentGrowthRate() {
    var properties = new FeeProjectionProperties();
    properties.setAnnualGrowthRate(null);

    assertThatThrownBy(properties::validateAssumptions)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("calculation.fee.projection.annual-growth-rate");
  }

  @ParameterizedTest
  @ValueSource(strings = {"-1", "-0.25", "0", "0.06", "1.5"})
  void shouldAcceptEveryGrowthRateFromMinusOneUpwards(String growthRate) {
    // -1 is degenerate but sound: the balance is wiped after year one, so the projection is a single annual fee
    assertThatCode(propertiesWith(Set.of(ONE_YR, TWENTY_YR), growthRate)::validateAssumptions)
        .doesNotThrowAnyException();
  }

  @Test
  void shouldPreferTheRequestedPeriods_whenAskedForSome() {
    var properties = new FeeProjectionProperties();

    assertThat(properties.periodsFor(new LinkedHashSet<>(Set.of(ONE_MTH)))).containsExactly(ONE_MTH);
    assertThat(properties.periodsFor(null)).containsExactly(ONE_YR, TEN_YR, TWENTY_YR);
    assertThat(properties.periodsFor(Set.of())).containsExactly(ONE_YR, TEN_YR, TWENTY_YR);
  }

  /** A caller asking for more columns than the defaults gets them: the server set is a fallback, not a ceiling. */
  @Test
  void shouldNotCapTheRequestedPeriods_atTheConfiguredDefaults() {
    var properties = new FeeProjectionProperties();
    var requested = new LinkedHashSet<>(java.util.List.of(ONE_MTH, ONE_YR, THREE_YR, TEN_YR, TWENTY_YR));

    assertThat(properties.periodsFor(requested)).containsExactlyElementsOf(requested);
  }

  private static FeeProjectionProperties propertiesWith(Set<TimePeriod> periods, String growthRate) {
    var properties = new FeeProjectionProperties();
    properties.setPeriods(periods);
    properties.setAnnualGrowthRate(new BigDecimal(growthRate));
    return properties;
  }
}
