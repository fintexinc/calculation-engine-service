package com.fintex.ce.service.impl.calculation.period.core;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.domain.calculation.PeriodBasedCalculation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.core.PeriodResDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.RequestValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PeriodAbstractServiceTest {

    @Test
    void perform_verifyDefineCalculationMethod() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(RequestValidator.class);
        final var sut = mock(PeriodAbstractService.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), requestValidator));

        final var dto = mock(PeriodsReqDTO.class);
        when(sut.defineCalculationMethod(any())).thenReturn(mock(PeriodBasedCalculation.class));

        doCallRealMethod().when(sut).perform(any());
        //ACT
        sut.perform(dto);

        //VERIFY
        verify(sut).defineCalculationMethod(dto);
    }

    @Test
    void perform_verifyValidate() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(RequestValidator.class);
        final var sut = mock(PeriodAbstractService.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), requestValidator));

        final var dto = mock(PeriodsReqDTO.class);
        when(sut.defineCalculationMethod(any())).thenReturn(mock(PeriodBasedCalculation.class));

        doCallRealMethod().when(sut).perform(any());
        //ACT
        sut.perform(dto);

        //VERIFY
        verify(requestValidator).validate(dto);
    }

    @Test
    void perform_verifyCalculate() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(RequestValidator.class);
        final var sut = mock(PeriodAbstractService.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), requestValidator));

        final var dto = mock(PeriodsReqDTO.class);
        final var periods = Set.of("e");
        when(dto.getPeriods()).thenReturn(periods);

        final var pCalculation = mock(PeriodBasedCalculation.class);
        when(sut.defineCalculationMethod(any())).thenReturn(pCalculation);

        doCallRealMethod().when(sut).perform(any());
        //ACT
        sut.perform(dto);

        //VERIFY
        verify(pCalculation).calculate(periods);
    }

    @Test
    void perform_checkResult() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(RequestValidator.class);
        final var sut = mock(PeriodAbstractService.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), requestValidator));

        final var dto = mock(PeriodsReqDTO.class);
        final var periods = Set.of("e");
        when(dto.getPeriods()).thenReturn(periods);

        final var pCalculation = mock(PeriodBasedCalculation.class);
        when(sut.defineCalculationMethod(any())).thenReturn(pCalculation);

        final var expected = mock(PeriodResDTO.class);
        when(pCalculation.calculate(any())).thenReturn(expected);

        doCallRealMethod().when(sut).perform(any());
        //ACT
        final PeriodResDTO actual = sut.perform(dto);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void buildCalculationDto_checkResult() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(RequestValidator.class);
        final var sut = mock(PeriodAbstractService.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), requestValidator));

        final var req = mock(PeriodsReqDTO.class);
        final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;
        final var monthlyReturns = mock(Returns.class);
        final var portfolioTotalReturns = mock(NavigableMap.class);

        when(req.getHoldings()).thenReturn(new ArrayList<>());
        when(req.getCurrency()).thenReturn(Currency.CAD);
        when(req.getCustomPed()).thenReturn(LocalDate.now().plusMonths(5));
        when(req.getCustomIntervalPsd()).thenReturn(LocalDate.now().minusMonths(3));

        when(monthlyReturnsService.getPortfolioMonthlyReturns(req.getHoldings(), Currency.CAD, ReturnFactorScale.SCALE_OF_TWO)).thenReturn(monthlyReturns);
        when(monthlyReturnsService.getWeightedAverageWithCpedValidation(monthlyReturns, req.getCustomPed())).thenReturn(portfolioTotalReturns);

        final CalculationDTO expected = new CalculationDTO();
        expected.setCipsd(req.getCustomIntervalPsd());
        expected.setWeightedAveragePortfolioReturns(portfolioTotalReturns);

        doCallRealMethod().when(sut).buildCalculationDto(any(), any());
        //ACT
        final CalculationDTO actual = sut.buildCalculationDto(req, returnFactorScale);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void buildCalculationDto_verifyGetPortfolioMonthlyReturns() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(RequestValidator.class);
        final var sut = mock(PeriodAbstractService.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), requestValidator));

        final var req = mock(PeriodsReqDTO.class);
        final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;
        final var monthlyReturns = mock(Returns.class);
        final var portfolioTotalReturns = mock(NavigableMap.class);

        when(req.getHoldings()).thenReturn(new ArrayList<>());
        when(req.getCurrency()).thenReturn(Currency.CAD);
        when(req.getCustomPed()).thenReturn(LocalDate.now().plusMonths(5));
        when(req.getCustomIntervalPsd()).thenReturn(LocalDate.now().minusMonths(3));

        when(monthlyReturnsService.getPortfolioMonthlyReturns(req.getHoldings(), Currency.CAD, ReturnFactorScale.SCALE_OF_TWO)).thenReturn(monthlyReturns);
        when(monthlyReturnsService.getWeightedAverageWithCpedValidation(monthlyReturns, req.getCustomPed())).thenReturn(portfolioTotalReturns);

        final CalculationDTO expected = new CalculationDTO();
        expected.setCipsd(req.getCustomIntervalPsd());
        expected.setWeightedAveragePortfolioReturns(portfolioTotalReturns);

        doCallRealMethod().when(sut).buildCalculationDto(any(), any());
        //ACT
        sut.buildCalculationDto(req, returnFactorScale);

        //VERIFY
        verify(monthlyReturnsService).getPortfolioMonthlyReturns(req.getHoldings(), Currency.CAD, ReturnFactorScale.SCALE_OF_TWO);
    }

    @Test
    void buildCalculationDto_verifyGetWeightedAverageWithCpedValidation() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(RequestValidator.class);
        final var sut = mock(PeriodAbstractService.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), requestValidator));

        final var req = mock(PeriodsReqDTO.class);
        final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;
        final var monthlyReturns = mock(Returns.class);
        final var portfolioTotalReturns = mock(NavigableMap.class);

        when(req.getHoldings()).thenReturn(new ArrayList<>());
        when(req.getCurrency()).thenReturn(Currency.CAD);
        when(req.getCustomPed()).thenReturn(LocalDate.now().plusMonths(5));
        when(req.getCustomIntervalPsd()).thenReturn(LocalDate.now().minusMonths(3));

        when(monthlyReturnsService.getPortfolioMonthlyReturns(req.getHoldings(), Currency.CAD, ReturnFactorScale.SCALE_OF_TWO)).thenReturn(monthlyReturns);
        when(monthlyReturnsService.getWeightedAverageWithCpedValidation(monthlyReturns, req.getCustomPed())).thenReturn(portfolioTotalReturns);

        final CalculationDTO expected = new CalculationDTO();
        expected.setCipsd(req.getCustomIntervalPsd());
        expected.setWeightedAveragePortfolioReturns(portfolioTotalReturns);

        doCallRealMethod().when(sut).buildCalculationDto(any(), any());
        //ACT
        sut.buildCalculationDto(req, returnFactorScale);

        //VERIFY
        verify(monthlyReturnsService).getWeightedAverageWithCpedValidation(monthlyReturns, req.getCustomPed());
    }

}