package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.util.ReturnFactorScale;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.model.util.BigDecimalConstants.TEN_THOUSAND;
import static org.assertj.core.api.Assertions.assertThat;

class GrowthOf10KCalculationServiceImplTest {

  private static final LocalDate JAN_2020 = LocalDate.of(2020, 1, 31);
  private static final LocalDate FEB_2020 = LocalDate.of(2020, 2, 29);
  private static final LocalDate DEC_2019 = LocalDate.of(2019, 12, 31);

  @Test
  void shouldReturnEmptyMap_whenCompoundGrowth10KInputIsNull() {
    TreeMap<LocalDate, BigDecimal> growth = GrowthOf10KCalculationServiceImpl.compoundGrowth10K(null,
        ReturnFactorScale.SCALE_OF_TWO);

    assertThat(growth).isEmpty();
  }

  @Test
  void shouldReturnEmptyMap_whenCompoundGrowth10KInputIsEmpty() {
    TreeMap<LocalDate, BigDecimal> growth = GrowthOf10KCalculationServiceImpl.compoundGrowth10K(new TreeMap<>(),
        ReturnFactorScale.SCALE_OF_TWO);

    assertThat(growth).isEmpty();
  }

  @Test
  void shouldSeedTenThousandOneMonthBeforeFirstReturn_whenCompoundGrowth10K() {
    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(JAN_2020, new BigDecimal("5.0")));

    TreeMap<LocalDate, BigDecimal> growth = GrowthOf10KCalculationServiceImpl.compoundGrowth10K(returns,
        ReturnFactorScale.SCALE_OF_TWO);

    // Seed lands at Dec 2019 (last day of month before first return).
    assertThat(growth).containsKey(DEC_2019);
    assertThat(growth.get(DEC_2019)).isEqualByComparingTo(TEN_THOUSAND);
  }

  @Test
  void shouldCompoundPercentInputViaScaleOfTwo_whenCompoundGrowth10KScaleIsScaleOfTwo() {
    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(
        JAN_2020, new BigDecimal("5.0"),
        FEB_2020, new BigDecimal("-2.0")));

    TreeMap<LocalDate, BigDecimal> growth = GrowthOf10KCalculationServiceImpl.compoundGrowth10K(returns,
        ReturnFactorScale.SCALE_OF_TWO);

    // Jan: 10,000 * (1 + 5/100) = 10,500
    assertThat(growth.get(JAN_2020)).isEqualByComparingTo("10500");
    // Feb: 10,500 * (1 - 2/100) = 10,290
    assertThat(growth.get(FEB_2020)).isEqualByComparingTo("10290");
  }

  @Test
  void shouldCompoundFactorInputAsIs_whenCompoundGrowth10KScaleIsAsIs() {
    // Regression: production callers (GrowthOf10K, MaxDrawdown, MarRatio) feed the weighted-average return series,
    // which the WeightedAverageComponent has already converted to factor form (e.g. 1.05). Passing AS_IS must multiply
    // by 1.05 directly; if SCALE_OF_TWO were re-applied, the factor would be (1.05 + 100)/100 = 1.0105 and the curve
    // would be nearly flat, masking real drawdowns.
    NavigableMap<LocalDate, BigDecimal> factorReturns = new TreeMap<>(Map.of(
        JAN_2020, new BigDecimal("1.05"),
        FEB_2020, new BigDecimal("0.98")));

    TreeMap<LocalDate, BigDecimal> growth = GrowthOf10KCalculationServiceImpl.compoundGrowth10K(factorReturns,
        ReturnFactorScale.AS_IS);

    // Jan: 10,000 * 1.05 = 10,500
    assertThat(growth.get(JAN_2020)).isEqualByComparingTo("10500");
    // Feb: 10,500 * 0.98 = 10,290
    assertThat(growth.get(FEB_2020)).isEqualByComparingTo("10290");
  }

  @Test
  void shouldStopAtLastInputMonth_whenCompoundGrowth10KAndNoFurtherReturns() {
    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(
        JAN_2020, new BigDecimal("5.0"),
        FEB_2020, new BigDecimal("0.0")));

    TreeMap<LocalDate, BigDecimal> growth = GrowthOf10KCalculationServiceImpl.compoundGrowth10K(returns,
        ReturnFactorScale.SCALE_OF_TWO);

    // Seed + Jan + Feb = 3 entries; no padding past Feb even if caller's window is longer.
    assertThat(growth).hasSize(3);
    assertThat(growth.lastKey()).isEqualTo(FEB_2020);
    assertThat(growth).doesNotContainValue(null);
  }
}
