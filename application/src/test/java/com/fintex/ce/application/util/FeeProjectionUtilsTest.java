package com.fintex.ce.application.util;

import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_MTH;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.TEN_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.THREE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.TWENTY_YR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeeProjectionUtilsTest {

  private static final BigDecimal SIX_PERCENT = new BigDecimal("0.06");

  @ParameterizedTest
  @CsvSource({
      "0.05,  THREE_YR,   3.152500000000000",
      "0.06,  ONE_YR,     1.000000000000000",
      "0.06,  TWO_YR,     2.060000000000000",
      "0.06,  TEN_YR,    13.180794942380833",
      "0.06,  TWENTY_YR, 36.785591203547333",
      "-0.10, THREE_YR,   2.710000000000000"
  })
  void shouldSumTheGrowingBalanceFactor_whenGrowthRateIsNonZero(BigDecimal growthRate, TimePeriod period,
      BigDecimal expectedFactor) {
    assertThat(FeeProjectionUtils.growthFactor(growthRate, period)).isEqualByComparingTo(expectedFactor);
  }

  /**
   * The whole-year factors are the ones the metric shipped with before periods were counted in months. Asserting them
   * unchanged is the guard that moving to months reshaped the contract without moving a single reported number.
   */
  @ParameterizedTest
  @CsvSource({"ONE_YR, 1.000000000000000", "TEN_YR, 13.180794942380833", "TWENTY_YR, 36.785591203547333"})
  void shouldKeepTheWholeYearFactorsUnchanged_afterTheMoveToMonths(TimePeriod period, BigDecimal expectedFactor) {
    assertThat(FeeProjectionUtils.growthFactor(SIX_PERCENT, period)).isEqualByComparingTo(expectedFactor);
  }

  @ParameterizedTest
  @CsvSource({"ONE_MTH, 1", "THREE_MTH, 3", "SIX_MTH, 6", "ONE_YR, 12", "TEN_YR, 120", "TWENTY_YR, 240"})
  void shouldDegenerateToTheMonthCountOverTwelve_whenGrowthRateIsZeroOrAbsent(TimePeriod period, int months) {
    BigDecimal expected = divide(BigDecimal.valueOf(months), TWELVE);

    assertThat(FeeProjectionUtils.growthFactor(BigDecimal.ZERO, period)).isEqualByComparingTo(expected);
    assertThat(FeeProjectionUtils.growthFactor(null, period)).isEqualByComparingTo(expected);
  }

  /**
   * A sub-year period pro-rates the first year, so it costs the same fraction of the annual fee whatever the balance is
   * doing — the balance has not had a year in which to grow yet.
   */
  @ParameterizedTest
  @CsvSource({"ONE_MTH, 0.083333333333333", "THREE_MTH, 0.25", "SIX_MTH, 0.5"})
  void shouldProRateTheFirstYear_whenPeriodIsShorterThanAYear(TimePeriod period, BigDecimal expectedFactor) {
    assertThat(FeeProjectionUtils.growthFactor(SIX_PERCENT, period)).isEqualByComparingTo(expectedFactor);
    assertThat(FeeProjectionUtils.growthFactor(BigDecimal.ZERO, period)).isEqualByComparingTo(expectedFactor);
    assertThat(FeeProjectionUtils.growthFactor(new BigDecimal("-0.25"), period)).isEqualByComparingTo(expectedFactor);
  }

  /**
   * The correspondence that makes the sub-year answers readable: a one-month projection is the {@code monthlyFee} the
   * same metric already reports, at any growth rate. Compared at output scale because {@code monthlyFee} is carried at
   * internal scale and only rounded on the way out.
   */
  @Test
  void shouldReproduceTheMonthlyFee_whenPeriodIsOneMonth() {
    for (String fee : List.of("4841", "1200", "1234.56")) {
      BigDecimal annualFee = new BigDecimal(fee);
      BigDecimal monthlyFee = toUserScale(divide(annualFee, TWELVE));

      assertThat(FeeProjectionUtils.spend(annualFee, SIX_PERCENT, ONE_MTH)).isEqualByComparingTo(monthlyFee);
      assertThat(FeeProjectionUtils.spend(annualFee, BigDecimal.ZERO, ONE_MTH)).isEqualByComparingTo(monthlyFee);
    }
    assertThat(FeeProjectionUtils.spend(new BigDecimal("1200"), SIX_PERCENT, ONE_MTH)).isEqualByComparingTo("100");
  }

  @Test
  void shouldMultiplyFeesByYears_whenBalanceIsFlat() {
    BigDecimal annualFee = new BigDecimal("4841");

    assertThat(FeeProjectionUtils.spend(annualFee, BigDecimal.ZERO, ONE_YR)).isEqualByComparingTo("4841");
    assertThat(FeeProjectionUtils.spend(annualFee, BigDecimal.ZERO, TEN_YR)).isEqualByComparingTo("48410");
    assertThat(FeeProjectionUtils.spend(annualFee, BigDecimal.ZERO, TWENTY_YR)).isEqualByComparingTo("96820");
  }

  @Test
  void shouldReturnTheAnnualFeeUnchanged_whenPeriodIsOneYear() {
    BigDecimal annualFee = new BigDecimal("1234.56");

    assertThat(FeeProjectionUtils.spend(annualFee, SIX_PERCENT, ONE_YR)).isEqualByComparingTo(annualFee);
    assertThat(FeeProjectionUtils.spend(annualFee, BigDecimal.ZERO, ONE_YR)).isEqualByComparingTo(annualFee);
    assertThat(FeeProjectionUtils.spend(annualFee, new BigDecimal("-0.25"), ONE_YR)).isEqualByComparingTo(annualFee);
  }

  @Test
  void shouldChargeMoreThanTheFlatCase_whenBalanceGrows() {
    BigDecimal annualFee = new BigDecimal("1000");

    BigDecimal flat = FeeProjectionUtils.spend(annualFee, BigDecimal.ZERO, TWENTY_YR);
    BigDecimal growing = FeeProjectionUtils.spend(annualFee, SIX_PERCENT, TWENTY_YR);

    assertThat(flat).isEqualByComparingTo("20000");
    assertThat(growing).isEqualByComparingTo("36785.5912035473");
    assertThat(growing).isGreaterThan(flat);
  }

  @Test
  void shouldChargeLessThanTheFlatCase_whenBalanceShrinks() {
    BigDecimal annualFee = new BigDecimal("1000");

    BigDecimal shrinking = FeeProjectionUtils.spend(annualFee, new BigDecimal("-0.10"), THREE_YR);

    assertThat(shrinking).isEqualByComparingTo("2710");
    assertThat(shrinking).isLessThan(FeeProjectionUtils.spend(annualFee, BigDecimal.ZERO, THREE_YR));
  }

  @Test
  void shouldMapEveryPeriodToNull_whenAnnualFeeIsAbsent() {
    Map<TimePeriod, BigDecimal> byPeriod = FeeProjectionUtils.byPeriod(null, SIX_PERCENT,
        List.of(ONE_YR, TEN_YR, TWENTY_YR));

    assertThat(byPeriod).hasSize(3).containsOnlyKeys(ONE_YR, TEN_YR, TWENTY_YR);
    assertThat(byPeriod.values()).containsOnlyNulls();
    assertThat(FeeProjectionUtils.spend(null, SIX_PERCENT, TEN_YR)).isNull();
  }

  @Test
  void shouldKeepPeriodOrderAndValues_whenProjectingSeveralPeriods() {
    Map<TimePeriod, BigDecimal> byPeriod = FeeProjectionUtils.byPeriod(new BigDecimal("100"), SIX_PERCENT,
        List.of(ONE_MTH, ONE_YR, TEN_YR, TWENTY_YR));

    assertThat(byPeriod.keySet()).containsExactly(ONE_MTH, ONE_YR, TEN_YR, TWENTY_YR);
    assertThat(byPeriod.get(ONE_MTH)).isEqualByComparingTo("8.3333333333");
    assertThat(byPeriod.get(ONE_YR)).isEqualByComparingTo("100");
    assertThat(byPeriod.get(TEN_YR)).isEqualByComparingTo("1318.0794942381");
    assertThat(byPeriod.get(TWENTY_YR)).isEqualByComparingTo("3678.5591203547");
    assertThat(byPeriod.get(TWENTY_YR)).isGreaterThan(byPeriod.get(TEN_YR));
  }

  @Test
  void shouldReturnAnEmptyMap_whenNoPeriodsAreConfigured() {
    assertThat(FeeProjectionUtils.byPeriod(new BigDecimal("100"), SIX_PERCENT, List.of())).isEmpty();
  }

  /**
   * A projection needs a length to project over. The fee contract already excludes the length-less periods, so this is
   * a guard against a wiring mistake rather than against bad input — hence a plain {@link IllegalArgumentException} and
   * no error code.
   */
  @ParameterizedTest
  @EnumSource(value = TimePeriod.class, names = {"YTD", "SI", "QTD", "OVERALL", "ITD", "CIPSD"})
  void shouldRejectThePeriod_whenItHasNoFixedLength(TimePeriod period) {
    assertThatThrownBy(() -> FeeProjectionUtils.spend(new BigDecimal("100"), SIX_PERCENT, period))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("fixed length")
        .hasMessageContaining(period.name());
  }

  @Test
  void shouldRejectNull_ratherThanFailWithANullPointer() {
    assertThatThrownBy(() -> FeeProjectionUtils.spend(new BigDecimal("100"), SIX_PERCENT, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("fixed length");
  }

  /**
   * Pins the formula against the reference design's own figures: its current-portfolio panel shows a $4,841 annual fee
   * alongside $60,500 at ten years and $156,800 at twenty. Those are 12.5x and 32.4x the annual fee, so the design
   * cannot be charging a flat balance (which would give 10x and 20x) nor compounding the fee as a drag (which would
   * give less than 10x and 20x) — it grows the balance at a shade under 5% and sums the fee charged each year.
   */
  @Test
  void shouldReproduceTheReferenceDesignFigures_whenBalanceGrowsJustUnderFivePercent() {
    BigDecimal annualFee = new BigDecimal("4841");
    BigDecimal growthRate = new BigDecimal("0.048");

    assertThat(FeeProjectionUtils.spend(annualFee, growthRate, TEN_YR))
        .isCloseTo(new BigDecimal("60500"), withinOnePercentOf("60500"));
    assertThat(FeeProjectionUtils.spend(annualFee, growthRate, TWENTY_YR))
        .isCloseTo(new BigDecimal("156800"), withinOnePercentOf("156800"));

    assertThat(FeeProjectionUtils.growthFactor(growthRate, TEN_YR)).isGreaterThan(BigDecimal.TEN);
    assertThat(FeeProjectionUtils.growthFactor(growthRate, TWENTY_YR)).isGreaterThan(BigDecimal.valueOf(20));
  }

  private static Offset<BigDecimal> withinOnePercentOf(String value) {
    return Offset.offset(new BigDecimal(value).movePointLeft(2));
  }
}
