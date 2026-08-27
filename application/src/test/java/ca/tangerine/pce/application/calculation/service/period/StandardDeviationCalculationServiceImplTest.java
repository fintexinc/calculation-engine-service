package ca.tangerine.pce.application.calculation.service.period;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import static ca.tangerine.pce.model.util.BigDecimalConstants.OUTPUT_SCALE;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.FIVE_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.TEN_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.THREE_YR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import ca.tangerine.pce.application.calculation.metric.StandardDeviationCalculation;
import ca.tangerine.pce.application.config.PeriodProperties;
import ca.tangerine.pce.application.returns.ReturnsSnapshot;
import ca.tangerine.pce.application.returns.WeightedAverageResult;
import ca.tangerine.pce.application.util.ReturnFactorScale;
import ca.tangerine.pce.model.domain.calculation.input.PeriodCalculationInput;
import ca.tangerine.pce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.CalculationException;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

class StandardDeviationCalculationServiceImplTest {

  @Test
  void shouldBuildStandardDeviationCalculation_whenWeightedAverageResultProvided() {
    var service = mock(StandardDeviationCalculationServiceImpl.class,
        withSettings().useConstructor(null, null, riskPeriods(ONE_YR, THREE_YR, FIVE_YR, TEN_YR)));
    var req = mock(PeriodCommand.class);
    var weightedAverageResult = mock(WeightedAverageResult.class);
    var snapshot = mock(ReturnsSnapshot.class);
    when(weightedAverageResult.snapshot()).thenReturn(snapshot);
    when(snapshot.warnings()).thenReturn(List.of());
    when(weightedAverageResult.weightedAverage()).thenReturn(new TreeMap<>());
    when(service.buildWeightedAverageResult(any(), any(), any())).thenReturn(weightedAverageResult);

    doCallRealMethod().when(service).perform(any(), any());
    List<Object> constructorArgs = new ArrayList<>();
    try (var ignored = mockConstruction(StandardDeviationCalculation.class,
        (mocked, context) -> constructorArgs.addAll(context.arguments()))) {
      service.perform(req, PortfolioBenchmarkReturns.EMPTY);
    }

    assertEquals(List.of(new PeriodCalculationInput(new TreeMap<>()), Set.of(ONE_YR, THREE_YR, FIVE_YR, TEN_YR),
        OUTPUT_SCALE),
        constructorArgs);
  }

  @Test
  void shouldCallBuildWeightedAverageResultWithScaleOfTwo_whenPerforming() {
    var service = mock(StandardDeviationCalculationServiceImpl.class,
        withSettings().useConstructor(null, null, new PeriodProperties()));
    var req = mock(PeriodCommand.class);
    var weightedAverageResult = mock(WeightedAverageResult.class);
    var snapshot = mock(ReturnsSnapshot.class);
    when(weightedAverageResult.snapshot()).thenReturn(snapshot);
    when(snapshot.warnings()).thenReturn(List.of());
    when(weightedAverageResult.weightedAverage()).thenReturn(new TreeMap<>());
    when(service.buildWeightedAverageResult(any(), any(), any())).thenReturn(weightedAverageResult);

    doCallRealMethod().when(service).perform(any(), any());
    try (var ignored = mockConstruction(StandardDeviationCalculation.class)) {
      service.perform(req, PortfolioBenchmarkReturns.EMPTY);
    }

    verify(service).buildWeightedAverageResult(req, ReturnFactorScale.SCALE_OF_TWO, PortfolioBenchmarkReturns.EMPTY);
  }

  @Test
  void shouldThrowCalculationException_whenSnapshotContainsFxRatesUnavailableWarning() {
    var service = mock(StandardDeviationCalculationServiceImpl.class,
        withSettings().useConstructor(null, null, new PeriodProperties()));
    var req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);

    var fxWarning = ErrorCode.FX_RATES_UNAVAILABLE.asNotification("XBAL", Currency.USD, Currency.CAD);
    var weightedAverageResult = mock(WeightedAverageResult.class);
    var snapshot = mock(ReturnsSnapshot.class);
    when(weightedAverageResult.snapshot()).thenReturn(snapshot);
    when(snapshot.warnings()).thenReturn(List.of(fxWarning));
    when(service.buildWeightedAverageResult(any(), any(), any())).thenReturn(weightedAverageResult);

    doCallRealMethod().when(service).perform(any(), any());

    CalculationException ex = assertThrows(CalculationException.class,
        () -> service.perform(req, PortfolioBenchmarkReturns.EMPTY));
    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FX_RATES_UNAVAILABLE);
  }

  /** The service reads its defaults off {@code risk-calculations}, so that is the only set worth populating here. */
  private static PeriodProperties riskPeriods(TimePeriod... periods) {
    var properties = new PeriodProperties();
    properties.setRiskCalculations(new LinkedHashSet<>(List.of(periods)));
    return properties;
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("cipsdOutsidePortfolioPerformanceRangeCases")
  void shouldThrowCipsdOutsideDataRangeError_whenCipsdIsOutsidePortfolioPerformanceRange(
      String name,
      LocalDate cipsd) {
    LocalDate performanceStartDate = LocalDate.of(2024, 1, 31);
    LocalDate performanceEndDate = LocalDate.of(2024, 12, 31);
    NavigableMap<LocalDate, BigDecimal> portfolioReturns = new TreeMap<>();
    portfolioReturns.put(performanceStartDate, BigDecimal.ONE);
    portfolioReturns.put(performanceEndDate, BigDecimal.ONE);
    var service = mock(StandardDeviationCalculationServiceImpl.class,
        withSettings().useConstructor(null, null, new PeriodProperties()));
    var command = mock(PeriodCommand.class);
    var weightedAverageResult = mock(WeightedAverageResult.class);
    var snapshot = mock(ReturnsSnapshot.class);
    when(command.getCustomIntervalPsd()).thenReturn(cipsd);
    when(weightedAverageResult.snapshot()).thenReturn(snapshot);
    when(snapshot.performanceStartDate()).thenReturn(performanceStartDate);
    when(snapshot.performanceEndDate()).thenReturn(performanceEndDate);
    when(snapshot.warnings()).thenReturn(List.of());
    when(weightedAverageResult.weightedAverage()).thenReturn(portfolioReturns);
    when(service.buildWeightedAverageResult(any(), any(), any())).thenReturn(weightedAverageResult);
    doCallRealMethod().when(service).perform(any(), any());

    CalculationException exception = assertThrows(CalculationException.class,
        () -> service.perform(command, PortfolioBenchmarkReturns.EMPTY));

    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CIPSD_OUTSIDE_DATA_RANGE_ERROR);
    assertThat(exception.getMessage()).isEqualTo(
        "CIPSD %s is outside the available monthly returns range [%s, %s]".formatted(
            cipsd, performanceStartDate, performanceEndDate));
    assertThat(exception.getMetadata())
        .containsEntry("param-1", cipsd)
        .containsEntry("param-2", performanceStartDate)
        .containsEntry("param-3", performanceEndDate);
  }

  @Test
  void shouldNotThrowException_whenCipsdIsWithinSnapshotRange_andWeightedReturnsStartLater() {
    LocalDate performanceStartDate = LocalDate.of(2024, 1, 31);
    LocalDate performanceEndDate = LocalDate.of(2024, 12, 31);
    NavigableMap<LocalDate, BigDecimal> portfolioReturns = new TreeMap<>();
    portfolioReturns.put(LocalDate.of(2024, 2, 29), BigDecimal.ONE);
    portfolioReturns.put(performanceEndDate, BigDecimal.ONE);
    var service = mock(StandardDeviationCalculationServiceImpl.class,
        withSettings().useConstructor(null, null, new PeriodProperties()));
    var command = mock(PeriodCommand.class);
    var weightedAverageResult = mock(WeightedAverageResult.class);
    var snapshot = mock(ReturnsSnapshot.class);
    when(command.getCustomIntervalPsd()).thenReturn(performanceStartDate);
    when(weightedAverageResult.snapshot()).thenReturn(snapshot);
    when(snapshot.performanceStartDate()).thenReturn(performanceStartDate);
    when(snapshot.performanceEndDate()).thenReturn(performanceEndDate);
    when(snapshot.warnings()).thenReturn(List.of());
    when(weightedAverageResult.weightedAverage()).thenReturn(portfolioReturns);
    when(service.buildWeightedAverageResult(any(), any(), any())).thenReturn(weightedAverageResult);
    doCallRealMethod().when(service).perform(any(), any());

    try (var ignored = mockConstruction(StandardDeviationCalculation.class)) {
      assertDoesNotThrow(() -> service.perform(command, PortfolioBenchmarkReturns.EMPTY));
    }
  }

  private static Stream<Arguments> cipsdOutsidePortfolioPerformanceRangeCases() {
    return Stream.of(
        Arguments.of("before performance start date", LocalDate.of(2023, 12, 31)),
        Arguments.of("after performance end date", LocalDate.of(2025, 1, 31)));
  }
}
