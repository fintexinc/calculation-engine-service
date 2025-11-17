package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.PeriodBasedCalculation;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.InformationRatioResDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.PeriodReqDtoForBenchmarkCalculationsValidator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.TreeMap;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class InformationRatioCalculationServiceImplTest {

    @Test
    void defineCalculationMethod_verifyBuildCalculationDto() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var defaultPeriod = mock(Set.class);
        final var reqDto = mock(PeriodsReqDTO.class);
        final var calculationDto = mock(BenchmarkCalculationDTO.class);
        final var requestValidator = mock(PeriodReqDtoForBenchmarkCalculationsValidator.class);

        final var sut = mock(InformationRatioCalculationServiceImpl.class,
                withSettings().useConstructor(monthlyReturnsService, defaultPeriod, requestValidator));

        when(calculationDto.getCipsd()).thenReturn(LocalDate.MIN);
        when(calculationDto.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap());
        when(sut.buildCalculationDto(reqDto, ReturnFactorScale.SCALE_OF_TWO))
                .thenReturn(calculationDto);
        doCallRealMethod().when(sut).defineCalculationMethod(any());

        //ACT
        final PeriodBasedCalculation<InformationRatioResDTO> actual = sut.defineCalculationMethod(reqDto);

        //VERIFY
        verify(sut).buildCalculationDto(reqDto, ReturnFactorScale.SCALE_OF_TWO);
    }

}