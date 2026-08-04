package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.returns.pipeline.CpsdCpedScaleParams;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.util.Growth10KHelper;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.returns.Growth10KResult;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.model.util.BigDecimalConstants.TEN_THOUSAND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GrowthOf10KCalculationServiceImplTest {

  private static final LocalDate JAN_2020 = LocalDate.of(2020, 1, 31);
  private static final LocalDate FEB_2020 = LocalDate.of(2020, 2, 29);
  private static final LocalDate MAR_2020 = LocalDate.of(2020, 3, 31);
  private static final LocalDate DEC_2019 = LocalDate.of(2019, 12, 31);

  private final PortfolioMonthlyReturnsContextProvider contextProvider = mock(
      PortfolioMonthlyReturnsContextProvider.class);
  private final PortfolioWeightedAverageWithCpsdAndCpedPipeline pipeline = mock(
      PortfolioWeightedAverageWithCpsdAndCpedPipeline.class);
  private final ReturnBenchmarkComparisonService returnBenchmarkComparisonService = mock(
      ReturnBenchmarkComparisonService.class);
  private final GrowthOf10KCalculationServiceImpl service = new GrowthOf10KCalculationServiceImpl(
      contextProvider, pipeline, returnBenchmarkComparisonService);

  @Test
  void shouldReportGrowthOf10KMetric() {
    assertThat(service.getMetric()).isEqualTo(CalculationMetric.GROWTH_OF_10K);
  }

  @Test
  void shouldForwardSnapshotWarnings_whenPipelineCollectedThem() {
    ReturnCommand command = new ReturnCommand();

    MonthlyReturnsContext context = mock(MonthlyReturnsContext.class);
    when(contextProvider.get(command.getHoldings(), command.getCurrency(), Map.of())).thenReturn(context);

    NavigableMap<LocalDate, BigDecimal> wa = new TreeMap<>();
    wa.put(JAN_2020, new BigDecimal("1.05"));
    wa.put(FEB_2020, new BigDecimal("0.98"));
    ReturnsSnapshot snapshot = ReturnsSnapshot.empty()
        .withAddedErrors(List.of(ErrorCode.CPED_AFTER_PORTFOLIO_PED.toException()));
    WeightedAverageResult<?> result = new WeightedAverageResult<>(wa, snapshot);
    when(pipeline.run(context, new CpsdCpedScaleParams(
        command.getCustomPsd(), command.getCustomPed(), ReturnFactorScale.SCALE_OF_TWO)))
        .thenReturn(result);

    Growth10KResult response = service.perform(command, PortfolioBenchmarkReturns.EMPTY);

    assertThat(response.getWarnings()).extracting(Notification::getCode).containsExactly("PFD-007");
    assertThat(response.getGrowth10k()).isNotEmpty();
  }

  @Test
  void shouldReturnEmptyMap_whenInputIsNull() {
    NavigableMap<LocalDate, BigDecimal> growth = Growth10KHelper.compoundGrowth10K(
        null, ReturnFactorScale.SCALE_OF_TWO);

    assertThat(growth).isEmpty();
  }

  @Test
  void shouldReturnEmptyMap_whenInputIsEmpty() {
    NavigableMap<LocalDate, BigDecimal> growth = Growth10KHelper.compoundGrowth10K(
        new TreeMap<>(), ReturnFactorScale.SCALE_OF_TWO);

    assertThat(growth).isEmpty();
  }

  @Test
  void shouldSeedTenThousandOneMonthBeforeFirstReturn_whenCompounding() {
    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(JAN_2020, new BigDecimal("5.0")));
    NavigableMap<LocalDate, BigDecimal> growth = Growth10KHelper.compoundGrowth10K(
        returns, ReturnFactorScale.SCALE_OF_TWO);

    assertThat(growth).containsKey(DEC_2019);
    assertThat(growth.get(DEC_2019)).isEqualByComparingTo(TEN_THOUSAND);
  }

  @Test
  void shouldCompoundPercentInputViaScaleOfTwo_whenScaleIsScaleOfTwo() {
    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(
        JAN_2020, new BigDecimal("5.0"),
        FEB_2020, new BigDecimal("-2.0")));
    NavigableMap<LocalDate, BigDecimal> growth = Growth10KHelper.compoundGrowth10K(
        returns, ReturnFactorScale.SCALE_OF_TWO);

    assertThat(growth.get(JAN_2020)).isEqualByComparingTo("10500");
    assertThat(growth.get(FEB_2020)).isEqualByComparingTo("10290");
  }

  @Test
  void shouldCompoundFactorInputAsIs_whenScaleIsAsIs() {
    NavigableMap<LocalDate, BigDecimal> factorReturns = new TreeMap<>(Map.of(
        JAN_2020, new BigDecimal("1.05"),
        FEB_2020, new BigDecimal("0.98")));
    NavigableMap<LocalDate, BigDecimal> growth = Growth10KHelper.compoundGrowth10K(
        factorReturns, ReturnFactorScale.AS_IS);

    assertThat(growth.get(JAN_2020)).isEqualByComparingTo("10500");
    assertThat(growth.get(FEB_2020)).isEqualByComparingTo("10290");
  }

  @Test
  void shouldStopAtLastInputMonth_whenNoFurtherReturns() {
    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(
        JAN_2020, new BigDecimal("5.0"),
        FEB_2020, new BigDecimal("0.0")));
    NavigableMap<LocalDate, BigDecimal> growth = Growth10KHelper.compoundGrowth10K(
        returns, ReturnFactorScale.SCALE_OF_TWO);

    assertThat(growth).hasSize(3);
    assertThat(growth.lastKey()).isEqualTo(FEB_2020);
    assertThat(growth).doesNotContainValue(null);
  }

  @Test
  void shouldIncludeSeedAndAllMonths_whenCompounding() {
    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(
        JAN_2020, new BigDecimal("5.0"),
        FEB_2020, new BigDecimal("-2.0"),
        MAR_2020, new BigDecimal("1.0")));

    NavigableMap<LocalDate, BigDecimal> growth = Growth10KHelper.compoundGrowth10K(
        returns, ReturnFactorScale.SCALE_OF_TWO);

    assertThat(growth).hasSize(4);
    assertThat(growth.firstKey()).isEqualTo(DEC_2019);
    assertThat(growth.lastKey()).isEqualTo(MAR_2020);
    assertThat(growth.get(DEC_2019)).isEqualByComparingTo(TEN_THOUSAND);
  }
}
