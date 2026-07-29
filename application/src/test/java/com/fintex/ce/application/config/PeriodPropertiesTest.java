package com.fintex.ce.application.config;

import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.convert.support.DefaultConversionService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fintex.wm.commons.domain.enumeration.TimePeriod.CIPSD;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_MTH;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.SEVEN_MTH;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.SI;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.SIX_MTH;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.TEN_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.THREE_MTH;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.THREE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.TWENTY_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.YTD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Typing the sets as {@link TimePeriod} splits what used to be one failure mode into two, and they are tested
 * separately: a value that is not a period at all now fails at <b>binding</b>, before the bean exists, while a value
 * that is a real period but wrong for the metric family fails at <b>validation</b>.
 */
class PeriodPropertiesTest {

  @Test
  void shouldBindTheCommaSeparatedFormTheYmlUses() {
    // the six keys are single strings, not yml lists, and must keep working that way so an existing diff stays valid
    PeriodProperties bound = bind(Map.of(
        "default.periods.risk-calculations", "12,36,60,120",
        "default.periods.rolling-calculations", "12, 36, 60, 120",
        "default.periods.trailing-total-returns", "1,3,6,12,36,60,120,YTD,SI",
        "default.periods.leading-total-returns", "1,3,6,12,36,60,120",
        "default.periods.information-ratio-returns", "12, 36, 60, 120",
        "default.periods.best-worst-periods", "12,36,60,120"));

    assertThat(bound.getRiskCalculations()).containsExactly(ONE_YR, THREE_YR, TimePeriod.FIVE_YR, TEN_YR);
    assertThat(bound.getTrailingTotalReturns())
        .containsExactly(ONE_MTH, THREE_MTH, SIX_MTH, ONE_YR, THREE_YR, TimePeriod.FIVE_YR, TEN_YR, YTD, SI);
    assertThatCode(bound::afterPropertiesSet).doesNotThrowAnyException();
  }

  @Test
  void shouldBindEitherWireForm_soAConfiguredPeriodCanBeNamedOrCounted() {
    PeriodProperties bound = bind(withRisk("ONE_YR, 36, TEN_YR, 240"));

    assertThat(bound.getRiskCalculations()).containsExactly(ONE_YR, THREE_YR, TEN_YR, TWENTY_YR);
  }

  @Test
  void shouldTolerateSpacesAroundEntries_becauseTheYmlIsWrittenThatWay() {
    // the binder trims, which is what retires the trimming the returns pipeline used to do at every consumer
    PeriodProperties bound = bind(withRisk("12, 36, 60, 120"));

    assertThat(bound.getRiskCalculations()).containsExactly(ONE_YR, THREE_YR, TimePeriod.FIVE_YR, TEN_YR);
  }

  @Test
  void shouldAcceptTwoHundredFortyMonths_theValueTheTwentyYearWindowNeeds() {
    assertThat(bind(withRisk("12,120,240")).getRiskCalculations()).contains(TWENTY_YR);
  }

  @Test
  void shouldAcceptDataDefinedPeriods_whereTheMetricFamilyResolvesThem() {
    PeriodProperties properties = propertiesWith(ONE_YR);
    properties.setTrailingTotalReturns(new LinkedHashSet<>(Set.of(ONE_YR, YTD, SI, CIPSD)));

    assertThatCode(properties::afterPropertiesSet).doesNotThrowAnyException();
  }

  /**
   * Leading returns are the exception among the return metrics: they count forward from the first observation, so a
   * length the data or the request resolves from the end of the series cannot be applied to them.
   */
  @Test
  void shouldFailValidation_whenLeadingReturnsNamesADataDefinedPeriod() {
    PeriodProperties properties = propertiesWith(ONE_YR);
    properties.setLeadingTotalReturns(new LinkedHashSet<>(List.of(ONE_YR, SI)));

    assertThatThrownBy(properties::afterPropertiesSet)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("default.periods.leading-total-returns")
        .hasMessageContaining(SI.name());
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "-12", "241", "18", "TWELVE", "MONTHS_7", "YEAR_TO_DATE", "SINCE_SOMETHING"})
  void shouldFailToBind_whenAValueIsNotAPeriodAtAll(String period) {
    assertThatThrownBy(() -> bind(withRisk("12," + period)))
        .isInstanceOf(BindException.class)
        .hasRootCauseInstanceOf(IllegalArgumentException.class);
  }

  /**
   * {@code SEVEN_MTH} is a perfectly real period — this is the case the old string-based check could not express,
   * because "is it a known value" and "may this metric family use it" were the same question.
   */
  @Test
  void shouldFailValidation_whenAPeriodIsRealButTooShortForTheMetricFamily() {
    PeriodProperties properties = propertiesWith(ONE_YR, SEVEN_MTH);

    assertThatThrownBy(properties::afterPropertiesSet)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("default.periods.risk-calculations")
        .hasMessageContaining(SEVEN_MTH.name());
  }

  @Test
  void shouldFailValidation_whenARiskFamilyKeyNamesADataDefinedPeriod() {
    PeriodProperties properties = propertiesWith(ONE_YR, YTD);

    assertThatThrownBy(properties::afterPropertiesSet)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(YTD.name());
  }

  @Test
  void shouldFailStartupOnAMissingKey_ratherThanReportNoPeriodsAtAll() {
    // the SpEL expressions used to fail placeholder resolution; an absent key must not degrade into an empty set
    PeriodProperties incomplete = new PeriodProperties();
    incomplete.setRiskCalculations(new LinkedHashSet<>(Set.of(ONE_YR)));

    assertThatThrownBy(incomplete::afterPropertiesSet)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("must list at least one period");
  }

  @Test
  void shouldValidateEverySetNotJustTheFirst() {
    PeriodProperties properties = propertiesWith(ONE_YR);
    properties.setBestWorstPeriods(new LinkedHashSet<>(Set.of(SEVEN_MTH)));

    assertThatThrownBy(properties::afterPropertiesSet)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("default.periods.best-worst-periods");
  }

  /** Mirrors the runtime wiring: {@link TimePeriodConverter} is what lets a month count bind to the enum. */
  private static PeriodProperties bind(Map<String, Object> properties) {
    DefaultConversionService conversionService = new DefaultConversionService();
    conversionService.addConverter(new TimePeriodConverter());
    return new Binder(
        List.of(new MapConfigurationPropertySource(properties)),
        null,
        conversionService)
        .bind("default.periods", Bindable.of(PeriodProperties.class))
        .orElseGet(PeriodProperties::new);
  }

  private static Map<String, Object> withRisk(String value) {
    Map<String, Object> properties = new LinkedHashMap<>(allKeys());
    properties.put("default.periods.risk-calculations", value);
    return properties;
  }

  private static Map<String, Object> allKeys() {
    Map<String, Object> properties = new LinkedHashMap<>();
    for (String key : new String[] {"risk-calculations", "rolling-calculations", "trailing-total-returns",
        "leading-total-returns", "information-ratio-returns", "best-worst-periods"}) {
      properties.put("default.periods." + key, "12");
    }
    return properties;
  }

  /** Every set populated so validation reaches the one under test rather than tripping on an empty sibling. */
  private static PeriodProperties propertiesWith(TimePeriod... riskPeriods) {
    var properties = new PeriodProperties();
    properties.setRiskCalculations(new LinkedHashSet<>(Set.of(riskPeriods)));
    properties.setRollingCalculations(new LinkedHashSet<>(Set.of(ONE_YR)));
    properties.setTrailingTotalReturns(new LinkedHashSet<>(Set.of(ONE_YR)));
    properties.setLeadingTotalReturns(new LinkedHashSet<>(Set.of(ONE_YR)));
    properties.setInformationRatioReturns(new LinkedHashSet<>(Set.of(ONE_YR)));
    properties.setBestWorstPeriods(new LinkedHashSet<>(Set.of(ONE_YR)));
    return properties;
  }
}
