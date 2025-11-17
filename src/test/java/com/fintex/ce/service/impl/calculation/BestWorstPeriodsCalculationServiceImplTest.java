package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.domain.calculation.BestWorstPeriodCalculation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.BestWorstPeriodsReqDTO;
import com.fintex.ce.dto.response.BestWorstPeriodsResponseDTO;
import com.fintex.ce.util.validation.request.BestWorstPeriodsReqValidator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.util.ReturnFactorScale.SCALE_OF_TWO;
import static com.fintex.ce.util.TestConstants.LOCAL_DATE_NOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class BestWorstPeriodsCalculationServiceImplTest {

    @Test
    void perform_verifyPortfolioPreValidation() {
        //SETUP
        final var requestValidator = mock(BestWorstPeriodsReqValidator.class);
        final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings().useConstructor(null, requestValidator));

        final var calculationDTO = mock(CalculationDTO.class);
        final var bestWorstPeriodsReqDTO = mock(BestWorstPeriodsReqDTO.class);
        final var holdings = List.of(mock(Holding.class));
        final var portfolioTotalReturns = mock(TreeMap.class);
        final BestWorstPeriodCalculation bestWorstPeriodCalculation = mock(BestWorstPeriodCalculation.class, withSettings()
                .useConstructor(portfolioTotalReturns, Set.of()));

        when(sut.buildWeightedAverageInputDto(any())).thenReturn(calculationDTO);
        when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());
        when(bestWorstPeriodsReqDTO.getHoldings()).thenReturn(holdings);
        when(sut.buildBestWorstPeriodCalculation(any(), any())).thenReturn(bestWorstPeriodCalculation);

        doCallRealMethod().when(sut).perform(bestWorstPeriodsReqDTO);
        //ACT
        sut.perform(bestWorstPeriodsReqDTO);

        //VERIFY
        verify(requestValidator).validate(bestWorstPeriodsReqDTO);

    }

    @Test
    void perform_checkResult() {
        //SETUP
        final var requestValidator = mock(BestWorstPeriodsReqValidator.class);
        final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings().useConstructor(null, requestValidator));

        final var calculationDTO = mock(CalculationDTO.class);
        final var bestWorstPeriodsReqDTO = mock(BestWorstPeriodsReqDTO.class);
        final var holdings = List.of(mock(Holding.class));
        final var portfolioTotalReturns = mock(TreeMap.class);
        final BestWorstPeriodCalculation bestWorstPeriodCalculation = mock(BestWorstPeriodCalculation.class, withSettings()
                .useConstructor(portfolioTotalReturns, Set.of()));

        final BestWorstPeriodsResponseDTO expected = mock(BestWorstPeriodsResponseDTO.class);
        when(bestWorstPeriodCalculation.calculate()).thenReturn(expected);
        when(sut.buildWeightedAverageInputDto(any())).thenReturn(calculationDTO);
        when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());
        when(bestWorstPeriodsReqDTO.getHoldings()).thenReturn(holdings);
        when(sut.buildBestWorstPeriodCalculation(any(), any())).thenReturn(bestWorstPeriodCalculation);

        doCallRealMethod().when(sut).perform(bestWorstPeriodsReqDTO);
        //ACT
        final BestWorstPeriodsResponseDTO actual = sut.perform(bestWorstPeriodsReqDTO);

        //VERIFY
        assertEquals(expected, actual);

    }

    @Test
    void perform_verifyBuildWeightedAverageInputDto() {
        //SETUP
        final var requestValidator = mock(BestWorstPeriodsReqValidator.class);
        final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings().useConstructor(null, requestValidator));

        final var calculationDTO = mock(CalculationDTO.class);
        final var portfolioTotalReturns = mock(TreeMap.class);
        final var bestWorstPeriodsReqDTO = mock(BestWorstPeriodsReqDTO.class);
        final var holdings = List.of(mock(Holding.class));
        final BestWorstPeriodCalculation bestWorstPeriodCalculation = mock(BestWorstPeriodCalculation.class, withSettings()
                .useConstructor(portfolioTotalReturns, Set.of()));

        when(sut.buildWeightedAverageInputDto(any())).thenReturn(calculationDTO);
        when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());
        when(bestWorstPeriodsReqDTO.getHoldings()).thenReturn(holdings);
        when(sut.buildBestWorstPeriodCalculation(any(), any())).thenReturn(bestWorstPeriodCalculation);

        doCallRealMethod().when(sut).perform(bestWorstPeriodsReqDTO);
        //ACT
        sut.perform(bestWorstPeriodsReqDTO);

        //VERIFY
        verify(sut).buildWeightedAverageInputDto(bestWorstPeriodsReqDTO);

    }

    @Test
    void perform_verifyBuildCalculation() {
        //SETUP
        final var requestValidator = mock(BestWorstPeriodsReqValidator.class);
        final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings().useConstructor(null, requestValidator));

        final var calculationDTO = mock(CalculationDTO.class);
        final var portfolioTotalReturns = mock(TreeMap.class);
        final var holdings = List.of(mock(Holding.class));
        final var bestWorstPeriodsReqDTO = mock(BestWorstPeriodsReqDTO.class);
        final BestWorstPeriodCalculation bestWorstPeriodCalculation = mock(BestWorstPeriodCalculation.class, withSettings()
                .useConstructor(portfolioTotalReturns, Set.of()));

        when(bestWorstPeriodsReqDTO.getHoldings()).thenReturn(holdings);
        when(sut.buildWeightedAverageInputDto(any())).thenReturn(calculationDTO);
        when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());
        when(sut.buildBestWorstPeriodCalculation(any(), any())).thenReturn(bestWorstPeriodCalculation);

        doCallRealMethod().when(sut).perform(bestWorstPeriodsReqDTO);
        //ACT
        sut.perform(bestWorstPeriodsReqDTO);

        //VERIFY
        verify(sut).buildBestWorstPeriodCalculation(bestWorstPeriodsReqDTO, calculationDTO);
    }


    @Test
    void buildBestWorstPeriodCalculation_verifyGetPeriods() {
        //SETUP
        final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class);

        final var calculationDTO = mock(CalculationDTO.class);
        final var portfolioTotalReturns = mock(TreeMap.class);
        final var holdings = List.of(mock(Holding.class));
        final var bestWorstPeriodsReqDTO = mock(BestWorstPeriodsReqDTO.class);

        when(bestWorstPeriodsReqDTO.getHoldings()).thenReturn(holdings);
        when(sut.buildWeightedAverageInputDto(any())).thenReturn(calculationDTO);
        when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());

        doCallRealMethod().when(sut).buildBestWorstPeriodCalculation(any(), any());
        //ACT
        sut.buildBestWorstPeriodCalculation(bestWorstPeriodsReqDTO, calculationDTO);

        //VERIFY
        verify(sut).getPeriods(bestWorstPeriodsReqDTO);
    }

    @Test
    void buildBestWorstPeriodCalculation_checkResult() {
        //SETUP
        final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class);

        final var calculationDTO = mock(CalculationDTO.class);
        final var holdings = List.of(mock(Holding.class));
        final var bestWorstPeriodsReqDTO = mock(BestWorstPeriodsReqDTO.class);

        when(bestWorstPeriodsReqDTO.getHoldings()).thenReturn(holdings);
        when(sut.buildWeightedAverageInputDto(any())).thenReturn(calculationDTO);
        when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.ONE)));
        final BestWorstPeriodCalculation expected = new BestWorstPeriodCalculation(calculationDTO.getWeightedAveragePortfolioReturns(),
                sut.getPeriods(bestWorstPeriodsReqDTO));

        doCallRealMethod().when(sut).buildBestWorstPeriodCalculation(any(), any());
        //ACT
        final BestWorstPeriodCalculation actual = sut.buildBestWorstPeriodCalculation(bestWorstPeriodsReqDTO, calculationDTO);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void getPeriods_CheckResultWithCustomPeriods() {
        //SETUP
        final var requestValidator = mock(BestWorstPeriodsReqValidator.class);
        final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings().useConstructor(null, requestValidator));

        final var bestWorstPeriodsReqDTO = mock(BestWorstPeriodsReqDTO.class);
        final var customPeriods = Set.of(12L, 24L);
        when(bestWorstPeriodsReqDTO.getBestWorstTimeIntervalPeriods()).thenReturn(customPeriods);

        doCallRealMethod().when(sut).getPeriods(any());
        //ACT
        final Set<Long> periods = sut.getPeriods(bestWorstPeriodsReqDTO);

        //VERIFY
        assertSame(customPeriods, periods);

    }

    @Test
    void getPeriods_CheckResultWithoutCustomPeriods() {
        //SETUP
        final var requestValidator = mock(BestWorstPeriodsReqValidator.class);
        final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings().useConstructor(null, requestValidator));

        final var bestWorstPeriodsReqDTO = mock(BestWorstPeriodsReqDTO.class);
        sut.defaultPeriods = Set.of(12L, 24L, 36L);

        when(bestWorstPeriodsReqDTO.getBestWorstTimeIntervalPeriods()).thenReturn(null);

        doCallRealMethod().when(sut).getPeriods(any());
        //ACT
        final Set<Long> periods = sut.getPeriods(bestWorstPeriodsReqDTO);

        //VERIFY
        assertSame(sut.defaultPeriods, periods);

    }

    @Test
    void bestWorstPeriodCalculation_calculateCheckResult() throws Exception {
        //SETUP
        final var requestValidator = mock(BestWorstPeriodsReqValidator.class);
        final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings().useConstructor(null, requestValidator));

        final var calculationDTO = mock(CalculationDTO.class);
        when(sut.buildWeightedAverageInputDto(any())).thenReturn(calculationDTO);
        when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());

        final var bestWorstPeriodsReqDTO = mock(BestWorstPeriodsReqDTO.class);
        final var holdings = List.of(mock(Holding.class));
        final var bestWorstPeriodCalculation = mock(BestWorstPeriodCalculation.class);
        final var resDTO = mock(BestWorstPeriodsResponseDTO.class);

        when(bestWorstPeriodsReqDTO.getHoldings()).thenReturn(holdings);
        when(bestWorstPeriodsReqDTO.getCurrency()).thenReturn(Currency.CAD);
        when(bestWorstPeriodsReqDTO.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
        when(bestWorstPeriodsReqDTO.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(1));
        when(sut.buildBestWorstPeriodCalculation(any(), any())).thenReturn(bestWorstPeriodCalculation);
        when(bestWorstPeriodCalculation.calculate()).thenReturn(resDTO);

        doCallRealMethod().when(sut).perform(any());
        //ACT
        final BestWorstPeriodsResponseDTO bestWorstPeriodsResponseDTO = sut.perform(bestWorstPeriodsReqDTO);

        //VERIFY
        assertSame(resDTO, bestWorstPeriodsResponseDTO);

    }

    @Test
    void buildWeightedAverageInputDto_verifyGetPortfolioMonthlyReturns() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(BestWorstPeriodsReqValidator.class);
        final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings()
                .useConstructor(monthlyReturnsService, requestValidator));

        final var holdings = List.of(mock(Holding.class));

        final var bestWorstPeriodsReqDTO = mock(BestWorstPeriodsReqDTO.class);
        when(bestWorstPeriodsReqDTO.getCurrency()).thenReturn(Currency.CAD);
        when(bestWorstPeriodsReqDTO.getHoldings()).thenReturn(holdings);
        when(bestWorstPeriodsReqDTO.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
        when(bestWorstPeriodsReqDTO.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

        doCallRealMethod().when(sut).buildWeightedAverageInputDto(any());
        //ACT
        sut.buildWeightedAverageInputDto(bestWorstPeriodsReqDTO);

        //VERIFY
        verify(monthlyReturnsService).getPortfolioMonthlyReturns(holdings, Currency.CAD, SCALE_OF_TWO);
    }

    @Test
    void buildWeightedAverageInputDto_verifyGetWeightedAverageWithCpsdAndCpedValidation() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(BestWorstPeriodsReqValidator.class);
        final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings()
                .useConstructor(monthlyReturnsService, requestValidator));

        final var holdings = List.of(mock(Holding.class));

        final var monthlyReturns = mock(Returns.class);
        when(monthlyReturnsService.getPortfolioMonthlyReturns(any(), any(), any())).thenReturn(monthlyReturns);
        when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any())).thenReturn(mock(NavigableMap.class));

        final var bestWorstPeriodsReqDTO = mock(BestWorstPeriodsReqDTO.class);
        when(bestWorstPeriodsReqDTO.getCurrency()).thenReturn(Currency.CAD);
        when(bestWorstPeriodsReqDTO.getHoldings()).thenReturn(holdings);
        when(bestWorstPeriodsReqDTO.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
        when(bestWorstPeriodsReqDTO.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

        doCallRealMethod().when(sut).buildWeightedAverageInputDto(any());
        //ACT
        sut.buildWeightedAverageInputDto(bestWorstPeriodsReqDTO);

        //VERIFY
        verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns,
                LOCAL_DATE_NOW.minusMonths(2), LOCAL_DATE_NOW);
    }

    @Test
    void buildWeightedAverageInputDto_checkResult() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(BestWorstPeriodsReqValidator.class);
        final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings()
                .useConstructor(monthlyReturnsService, requestValidator));

        final var holdings = List.of(mock(Holding.class));

        final var portfolioTotalReturns = mock(NavigableMap.class);
        final var expected = new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns);
        when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any())).thenReturn(portfolioTotalReturns);

        final var bestWorstPeriodsReqDTO = mock(BestWorstPeriodsReqDTO.class);
        when(bestWorstPeriodsReqDTO.getCurrency()).thenReturn(Currency.CAD);
        when(bestWorstPeriodsReqDTO.getHoldings()).thenReturn(holdings);
        when(bestWorstPeriodsReqDTO.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
        when(bestWorstPeriodsReqDTO.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

        doCallRealMethod().when(sut).buildWeightedAverageInputDto(any());
        //ACT
        final CalculationDTO actual = sut.buildWeightedAverageInputDto(bestWorstPeriodsReqDTO);

        //VERIFY
        assertEquals(expected, actual);
    }

}
