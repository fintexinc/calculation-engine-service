package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.InformationRatioCalculation;
import com.fintex.ce.application.returns.BenchmarkMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.TreeMap;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@Disabled("metric unsupported")
class InformationRatioCalculationServiceImplTest {

  @Test
  void shouldPerform_whenVerifyBuildPeriodCalculationInput() {
    // SETUP
    final var portfolioProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
    final var benchmarkProvider = mock(BenchmarkMonthlyReturnsContextProvider.class);
    final var defaultPeriod = mock(Set.class);
    final var command = mock(PeriodCommand.class);
    final var context = mock(BenchmarkPeriodCalculationInput.class);

    final var service = mock(InformationRatioCalculationServiceImpl.class,
        withSettings().useConstructor(portfolioProvider, benchmarkProvider, null, null, defaultPeriod));

    when(context.getCipsd()).thenReturn(LocalDate.MIN);
    when(context.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap());
    when(service.buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO, PortfolioBenchmarkReturns.EMPTY))
        .thenReturn(context);
    doCallRealMethod().when(service).perform(any(), any());

    // ACT
    try (var ignored = mockConstruction(InformationRatioCalculation.class)) {
      service.perform(command, PortfolioBenchmarkReturns.EMPTY);
    }

    // VERIFY
    verify(service).buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO,
        PortfolioBenchmarkReturns.EMPTY);
  }

}
