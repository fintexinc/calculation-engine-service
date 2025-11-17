package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.domain.calculation.AnnualReturnCalculation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.ReturnReqDTO;
import com.fintex.ce.dto.response.AnnualReturnResDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.util.validation.request.ReturnReqDtoValidator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.util.ReturnFactorScale.SCALE_OF_TWO;
import static com.fintex.ce.util.TestConstants.LOCAL_DATE_NOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class AnnualReturnServiceImplTest {

    @Test
    void calculate_verifyValidateHoldings() {
        //SETUP
        final ReturnReqDtoValidator requestValidator = mock(ReturnReqDtoValidator.class);
        final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);

        final var sut = mock(AnnualReturnServiceImpl.class, withSettings().useConstructor(monthlyReturnsService, requestValidator));

        final var returnReqDTO = mock(ReturnReqDTO.class);
        final var holdings = List.of(mock(Holding.class));
        final var calculationDTO = mock(CalculationDTO.class);

        when(returnReqDTO.getHoldings()).thenReturn(holdings);
        when(sut.buildWeightedAverageInputDto(any())).thenReturn(calculationDTO);
        when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());
        when(sut.buildAnnualReturnCalculation(any())).thenReturn(mock(AnnualReturnCalculation.class));

        doCallRealMethod().when(sut).perform(returnReqDTO);
        //ACT
        sut.perform(returnReqDTO);

        //VERIFY
        verify(requestValidator).validate(returnReqDTO);
    }

    @Test
    void perform_verifyBuildAnnualReturnCalculation() {
        //SETUP
        final ReturnReqDtoValidator requestValidator = mock(ReturnReqDtoValidator.class);
        final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var sut = mock(AnnualReturnServiceImpl.class, withSettings().useConstructor(monthlyReturnsService, requestValidator));

        final var returnReqDTO = mock(ReturnReqDTO.class);
        final var holdings = List.of(mock(Holding.class));
        final var calculationDTO = mock(CalculationDTO.class);

        when(returnReqDTO.getHoldings()).thenReturn(holdings);
        when(sut.buildWeightedAverageInputDto(any())).thenReturn(calculationDTO);
        when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());
        when(sut.buildAnnualReturnCalculation(any())).thenReturn(mock(AnnualReturnCalculation.class));

        doCallRealMethod().when(sut).perform(returnReqDTO);
        //ACT
        sut.perform(returnReqDTO);

        //VERIFY
        verify(sut).buildAnnualReturnCalculation(calculationDTO);
    }

    @Test
    void calculate_verifyBuildWeightedAverageInputDto() {
        //SETUP
        final ReturnReqDtoValidator requestValidator = mock(ReturnReqDtoValidator.class);
        final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var sut = mock(AnnualReturnServiceImpl.class, withSettings().useConstructor(monthlyReturnsService, requestValidator));

        final var returnReqDTO = mock(ReturnReqDTO.class);
        final var holdings = List.of(mock(Holding.class));
        when(returnReqDTO.getHoldings()).thenReturn(holdings);
        when(returnReqDTO.getCurrency()).thenReturn(Currency.CAD);
        when(returnReqDTO.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);

        final var calculationDTO = mock(CalculationDTO.class);
        when(sut.buildWeightedAverageInputDto(any())).thenReturn(calculationDTO);
        when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());

        final var annual = mock(AnnualReturnCalculation.class);
        final var resDTO = mock(AnnualReturnResDTO.class);
        when(annual.calculate()).thenReturn(resDTO);
        when(sut.buildAnnualReturnCalculation(any())).thenReturn(annual);

        doCallRealMethod().when(sut).perform(returnReqDTO);
        //ACT
        final AnnualReturnResDTO actual = sut.perform(returnReqDTO);

        //VERIFY
        assertSame(actual, resDTO);

    }

    @Test
    void buildWeightedAverageInputDto_verifyBuildCalculationDto() {
        //SETUP
        final ReturnReqDtoValidator requestValidator = mock(ReturnReqDtoValidator.class);
        final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var sut = mock(AnnualReturnServiceImpl.class, withSettings().useConstructor(monthlyReturnsService, requestValidator));
        final var holdings = List.of(mock(Holding.class));
        final var annual = mock(ReturnReqDTO.class);
        final var monthlyReturns = mock(Returns.class);
        when(annual.getCurrency()).thenReturn(Currency.CAD);
        when(annual.getHoldings()).thenReturn(holdings);
        when(annual.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
        when(annual.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));
        when(monthlyReturnsService.getPortfolioMonthlyReturns(any(), any(), any())).thenReturn(monthlyReturns);

        doCallRealMethod().when(sut).buildWeightedAverageInputDto(any());
        //ACT
        sut.buildWeightedAverageInputDto(annual);

        //VERIFY
        verify(monthlyReturnsService).getPortfolioMonthlyReturns(holdings, Currency.CAD, SCALE_OF_TWO);
    }

    @Test
    void buildWeightedAverageInputDto_checkResult() {
        //SETUP
        final ReturnReqDtoValidator requestValidator = mock(ReturnReqDtoValidator.class);
        final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var sut = mock(AnnualReturnServiceImpl.class, withSettings().useConstructor(monthlyReturnsService, requestValidator));

        final var holdings = List.of(mock(Holding.class));
        final NavigableMap map = new TreeMap<>();
        final var monthlyReturns = mock(Returns.class);

        when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any())).thenReturn(map);
        when(monthlyReturnsService.getPortfolioMonthlyReturns(any(), any(), any())).thenReturn(monthlyReturns);

        final var annual = mock(ReturnReqDTO.class);
        when(annual.getCurrency()).thenReturn(Currency.CAD);
        when(annual.getHoldings()).thenReturn(holdings);
        when(annual.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
        when(annual.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

        doCallRealMethod().when(sut).buildWeightedAverageInputDto(any());
        //ACT
        final CalculationDTO actual = sut.buildWeightedAverageInputDto(annual);

        //VERIFY
        final var expected = new CalculationDTO().setWeightedAveragePortfolioReturns(map).setWarnings(List.of());
        assertEquals(expected, actual);
    }

    @Test
    void buildWeightedAverageInputDto_verifyGetWeightedAverageWithCpsdAndCpedValidation() {
        //SETUP
        final ReturnReqDtoValidator requestValidator = mock(ReturnReqDtoValidator.class);
        final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var sut = mock(AnnualReturnServiceImpl.class, withSettings().useConstructor(monthlyReturnsService, requestValidator));

        final var holdings = List.of(mock(Holding.class));
        final var monthlyReturns = mock(Returns.class);

        when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(Currency.class), eq(SCALE_OF_TWO))).thenReturn(monthlyReturns);

        final var annual = mock(ReturnReqDTO.class);
        when(annual.getCurrency()).thenReturn(Currency.CAD);
        when(annual.getHoldings()).thenReturn(holdings);
        when(annual.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
        when(annual.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

        doCallRealMethod().when(sut).buildWeightedAverageInputDto(any());
        //ACT
        sut.buildWeightedAverageInputDto(annual);

        //VERIFY
        verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, LOCAL_DATE_NOW.minusMonths(2), LOCAL_DATE_NOW);
    }

    @Test
    void buildWeightedAverageInputDto_verifyGetPortfolioMonthlyReturns() {
        //SETUP
        final ReturnReqDtoValidator requestValidator = mock(ReturnReqDtoValidator.class);
        final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var sut = mock(AnnualReturnServiceImpl.class, withSettings().useConstructor(monthlyReturnsService, requestValidator));

        final var holdings = List.of(mock(Holding.class));
        final var monthlyReturns = mock(Returns.class);

        when(monthlyReturnsService.getPortfolioMonthlyReturns(any(), any(), any())).thenReturn(monthlyReturns);

        final var annual = mock(ReturnReqDTO.class);
        when(annual.getCurrency()).thenReturn(Currency.CAD);
        when(annual.getHoldings()).thenReturn(holdings);
        when(annual.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
        when(annual.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

        doCallRealMethod().when(sut).buildWeightedAverageInputDto(any());
        //ACT
        sut.buildWeightedAverageInputDto(annual);

        //VERIFY
        verify(monthlyReturnsService).getPortfolioMonthlyReturns(holdings, Currency.CAD, SCALE_OF_TWO);
    }

    @Test
    void buildAnnualReturnCalculation_verify() {
        try (var mockedAnnualReturnCalculation = mockConstruction(AnnualReturnCalculation.class)) {
            //SETUP
            final var sut = mock(AnnualReturnServiceImpl.class);
            final var input = mock(CalculationDTO.class);
            final var map = mock(NavigableMap.class);

            when(input.getWeightedAveragePortfolioReturns()).thenReturn(map);
            doCallRealMethod().when(sut).buildAnnualReturnCalculation(any());

            //ACT
            final AnnualReturnCalculation actual = sut.buildAnnualReturnCalculation(input);

            //VERIFY
            final List<AnnualReturnCalculation> constructed = mockedAnnualReturnCalculation.constructed();

            verify(input).getWeightedAveragePortfolioReturns();
            assertEquals(1, constructed.size());
            assertTrue(constructed.contains(actual));
        }
    }

    @Test
    void buildWeightedAverageInputDto_checkMonthlyReturnsWarnings() {
        //SETUP
        final ReturnReqDtoValidator requestValidator = mock(ReturnReqDtoValidator.class);
        final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var sut = mock(AnnualReturnServiceImpl.class, withSettings().useConstructor(monthlyReturnsService, requestValidator));

        final var holdings = List.of(mock(Holding.class));
        final NavigableMap map = new TreeMap<>();
        final var monthlyReturns = mock(Returns.class);
        final var warnings = List.of(mock(Warning.class));

        when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any())).thenReturn(map);
        when(monthlyReturnsService.getPortfolioMonthlyReturns(any(), any(), any())).thenReturn(monthlyReturns);
        when(monthlyReturns.getErrorsAsWarnings()).thenReturn(warnings);

        final var annual = mock(ReturnReqDTO.class);
        when(annual.getCurrency()).thenReturn(Currency.CAD);
        when(annual.getHoldings()).thenReturn(holdings);
        when(annual.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
        when(annual.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

        doCallRealMethod().when(sut).buildWeightedAverageInputDto(any());
        //ACT
        final CalculationDTO actual = sut.buildWeightedAverageInputDto(annual);

        //VERIFY
        final var expected = new CalculationDTO().setWeightedAveragePortfolioReturns(map).setWarnings(warnings);
        assertEquals(expected, actual);
    }

}