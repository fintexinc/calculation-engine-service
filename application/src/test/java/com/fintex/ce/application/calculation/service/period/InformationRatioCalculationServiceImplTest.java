package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.domain.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.model.result.InformationRatioResult;
import com.fintex.ce.util.ReturnFactorScale;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class InformationRatioCalculationServiceImplTest {

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildCalculationDto() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriod = mock(Set.class);
    final var reqDto = mock(PeriodCommand.class);
    final var calculationDto = mock(BenchmarkCalculationDTO.class);

    final var sut = mock(InformationRatioCalculationServiceImpl.class,
        withSettings().useConstructor(monthlyReturnsService, defaultPeriod));

    when(calculationDto.getCipsd()).thenReturn(LocalDate.MIN);
    when(calculationDto.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap());
    when(sut.buildCalculationDto(reqDto, ReturnFactorScale.SCALE_OF_TWO))
        .thenReturn(calculationDto);
    doCallRealMethod().when(sut).defineCalculationMethod(any());

    // ACT
    final PeriodCalculationAbstract<InformationRatioResult, ?> actual = sut.defineCalculationMethod(reqDto);

    // VERIFY
    verify(sut).buildCalculationDto(reqDto, ReturnFactorScale.SCALE_OF_TWO);
  }

}