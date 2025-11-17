package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.domain.calculation.CorrelationCalculation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.CorrelationReqValidator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CorrelationServiceImplTest {

    @Test
    void defineCalculationMethod_verifyGetPortfolioMonthlyReturns() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var sut = mock(CorrelationServiceImpl.class,
                withSettings().useConstructor(Set.of(), null, monthlyReturnsService));

        final Returns monthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);
        final Map baseTotalReturns = mock(Map.class);
        final NavigableMap portfolioTotalReturns = mock(NavigableMap.class);

        final var reqDTO = mock(PeriodsReqDTO.class);
        final List holdings = mock(List.class);
        when(reqDTO.getHoldings()).thenReturn(holdings);
        when(reqDTO.getCustomIntervalPsd()).thenReturn(LocalDate.now());
        when(reqDTO.getCustomPed()).thenReturn(LocalDate.now().minusMonths(1));
        when(reqDTO.getCurrency()).thenReturn(Currency.CAD);

        when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);

        when(monthlyReturns
                .validateCped(reqDTO.getCustomPed())
                .validateReturns()
                .cutByCpedIfCpedEmptyCutByPed(reqDTO.getCustomPed())
                .fxRatesApplied()
                .getReturnsMap()).thenReturn(baseTotalReturns);

        when(monthlyReturns.cutByPsd().getWeightedAverage()).thenReturn(portfolioTotalReturns);

        doCallRealMethod().when(sut).defineCalculationMethod(any());
        //ACT
        sut.defineCalculationMethod(reqDTO);

        //VERIFY
        verify(monthlyReturnsService).getPortfolioMonthlyReturns(holdings, Currency.CAD, ReturnFactorScale.SCALE_OF_TWO);
    }

    @Test
    void defineCalculationMethod_checkResult() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var sut = mock(CorrelationServiceImpl.class,
                withSettings().useConstructor(Set.of(), null, monthlyReturnsService));

        final Returns monthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);
        final Map baseTotalReturns = mock(Map.class);
        final NavigableMap portfolioTotalReturns = mock(NavigableMap.class);

        final var reqDTO = mock(PeriodsReqDTO.class);
        final List holdings = mock(List.class);
        when(reqDTO.getHoldings()).thenReturn(holdings);
        when(reqDTO.getCustomIntervalPsd()).thenReturn(LocalDate.now());
        when(reqDTO.getCustomPed()).thenReturn(LocalDate.now().minusMonths(1));
        when(reqDTO.getCurrency()).thenReturn(Currency.CAD);

        when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);

        when(monthlyReturns
                .validateCped(reqDTO.getCustomPed())
                .cutByCpedIfCpedEmptyCutByPed(reqDTO.getCustomPed())
                .fxRatesApplied()
                .getReturnsMap()).thenReturn(baseTotalReturns);

        when(monthlyReturns.cutByPsd().getWeightedAverage()).thenReturn(portfolioTotalReturns);

        final CalculationDTO calculationDTO = new CalculationDTO()
                .setCipsd(reqDTO.getCustomIntervalPsd())
                .setWeightedAveragePortfolioReturns(portfolioTotalReturns);
        final var expected = new CorrelationCalculation(calculationDTO, baseTotalReturns, Set.of());

        doCallRealMethod().when(sut).defineCalculationMethod(any());
        //ACT
        final var actual = sut.defineCalculationMethod(reqDTO);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void defineCalculationMethod_verifyReqDtoSetReqCurrencyToCashHolding() {
        //SETUP
        final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var sut = mock(CorrelationServiceImpl.class,
                withSettings().useConstructor(null, null, monthlyReturnsService));
        final var reqDTO = mock(PeriodsReqDTO.class);
        CalculationDTO inputDTO = mock(CalculationDTO.class);

        when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any()))
                .thenReturn(mock(Returns.class, RETURNS_SELF));

        when(sut.buildCalculationDto(any(), any())).thenReturn(inputDTO);
        when(inputDTO.getCipsd()).thenReturn(LocalDate.now());

        doCallRealMethod().when(sut).defineCalculationMethod(any());
        //ACT
        sut.defineCalculationMethod(reqDTO);

        //VERIFY
        verify(reqDTO).setReqCurrencyToCashHolding();
    }

    @Test
    void perform_verifyValidateCorrelationCalculation() {
        //SETUP
        final var requestValidator = mock(CorrelationReqValidator.class);
        final var sut = mock(CorrelationServiceImpl.class, withSettings().useConstructor(Set.of(), requestValidator, null));

        final var periodsReqDTO = mock(PeriodsReqDTO.class);

        when(sut.defineCalculationMethod(any())).thenReturn(mock(CorrelationCalculation.class));
        doCallRealMethod().when(sut).perform(any());

        //ACT
        sut.perform(periodsReqDTO);

        //VERIFY
        verify(requestValidator).validate(periodsReqDTO);
    }

    @Test
    void perform_verifyDefineCalculationMethod() {
        //SETUP
        final var requestValidator = mock(CorrelationReqValidator.class);
        final var sut = mock(CorrelationServiceImpl.class, withSettings().useConstructor(Set.of(), requestValidator, null));

        final var periodsReqDTO = mock(PeriodsReqDTO.class);


        when(sut.defineCalculationMethod(any())).thenReturn(mock(CorrelationCalculation.class));
        doCallRealMethod().when(sut).perform(any());

        //ACT
        sut.perform(periodsReqDTO);

        //VERIFY
        verify(sut).defineCalculationMethod(periodsReqDTO);
    }

    @Test
    void perform_verifyCalculate() {
        //SETUP
        final var requestValidator = mock(CorrelationReqValidator.class);
        final var sut = mock(CorrelationServiceImpl.class, withSettings().useConstructor(Set.of(), requestValidator, null));

        final var periodsReqDTO = mock(PeriodsReqDTO.class);
        final var set = mock(Set.class);
        final var correlationCalculation = mock(CorrelationCalculation.class);

        when(periodsReqDTO.getPeriods()).thenReturn(set);
        when(sut.defineCalculationMethod(any())).thenReturn(correlationCalculation);
        doCallRealMethod().when(sut).perform(any());

        //ACT
        sut.perform(periodsReqDTO);

        //VERIFY
        verify(correlationCalculation).calculate(set);
    }
}