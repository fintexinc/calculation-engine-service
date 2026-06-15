package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.StandardDeviationCalculation;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.model.util.BigDecimalConstants.OUTPUT_SCALE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class StandardDeviationCalculationServiceImplTest {

  @Test
  void shouldDefineCalculationMethod_whenWeightedAverageResultProvided() {
    var service = mock(StandardDeviationCalculationServiceImpl.class,
        withSettings().useConstructor(null, null, Set.of("12", "36", "60", "120")));
    var req = mock(PeriodCommand.class);
    var weightedAverageResult = mock(WeightedAverageResult.class);
    var snapshot = mock(com.fintex.ce.application.returns.ReturnsSnapshot.class);
    when(weightedAverageResult.snapshot()).thenReturn(snapshot);
    when(snapshot.warnings()).thenReturn(List.of());
    when(weightedAverageResult.weightedAverage()).thenReturn(new TreeMap<>());
    when(service.buildWeightedAverageResult(any(), any())).thenReturn(weightedAverageResult);

    var expected = StandardDeviationCalculation.builder()
        .input(new PeriodCalculationInput(new TreeMap<>()))
        .defaultPeriods(Set.of("12", "36", "60", "120"))
        .scale(OUTPUT_SCALE)
        .build();

    doCallRealMethod().when(service).defineCalculationMethod(any());
    StandardDeviationCalculation actual = service.defineCalculationMethod(req);

    assertEquals(expected, actual);
  }

  @Test
  void shouldCallBuildWeightedAverageResultWithScaleOfTwo_whenDefiningCalculationMethod() {
    var service = mock(StandardDeviationCalculationServiceImpl.class,
        withSettings().useConstructor(null, null, Set.of()));
    var req = mock(PeriodCommand.class);
    var weightedAverageResult = mock(WeightedAverageResult.class);
    var snapshot = mock(com.fintex.ce.application.returns.ReturnsSnapshot.class);
    when(weightedAverageResult.snapshot()).thenReturn(snapshot);
    when(snapshot.warnings()).thenReturn(List.of());
    when(weightedAverageResult.weightedAverage()).thenReturn(new TreeMap<>());
    when(service.buildWeightedAverageResult(any(), any())).thenReturn(weightedAverageResult);

    doCallRealMethod().when(service).defineCalculationMethod(any());
    service.defineCalculationMethod(req);

    verify(service).buildWeightedAverageResult(req, ReturnFactorScale.SCALE_OF_TWO);
  }

  @Test
  void shouldThrowCalculationException_whenSnapshotContainsFxRatesUnavailableWarning() {
    var service = mock(StandardDeviationCalculationServiceImpl.class,
        withSettings().useConstructor(null, null, Set.of()));
    var req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);

    var fxWarning = ErrorCode.FX_RATES_UNAVAILABLE.asNotification("XBAL", Currency.USD, Currency.CAD);
    var weightedAverageResult = mock(WeightedAverageResult.class);
    var snapshot = mock(com.fintex.ce.application.returns.ReturnsSnapshot.class);
    when(weightedAverageResult.snapshot()).thenReturn(snapshot);
    when(snapshot.warnings()).thenReturn(List.of(fxWarning));
    when(service.buildWeightedAverageResult(any(), any())).thenReturn(weightedAverageResult);

    doCallRealMethod().when(service).defineCalculationMethod(any());

    CalculationException ex = assertThrows(CalculationException.class,
        () -> service.defineCalculationMethod(req));
    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FX_RATES_UNAVAILABLE);
  }
}
