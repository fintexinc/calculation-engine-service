package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.domain.calculation.LeadingTotalReturnsCalculation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.LeadingTotalReturnPeriodsReqDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.util.ComparisonUtils;
import com.fintex.ce.util.validation.request.LeadingTotalReturnsReqValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.util.ReturnFactorScale.SCALE_OF_TWO;
import static com.fintex.ce.util.TestConstants.LOCAL_DATE_NOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class LeadingTotalReturnsCalculationServiceImplTest {

    @Test
    void perform_verifyValidate() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(LeadingTotalReturnsReqValidator.class);
        final var sut = mock(LeadingTotalReturnsCalculationServiceImpl.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), requestValidator));

        final var reqDTO = mock(LeadingTotalReturnPeriodsReqDTO.class);
        final var leadingTotalReturnCalculation = mock(LeadingTotalReturnsCalculation.class);

        when(sut.defineCalculationMethod(any())).thenReturn(leadingTotalReturnCalculation);

        doCallRealMethod().when(sut).perform(any());
        //ACT
        sut.perform(reqDTO);

        //VERIFY
        verify(requestValidator).validate(reqDTO);
    }

    @Test
    void perform_verifyDefineCalculationMethod() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(LeadingTotalReturnsReqValidator.class);
        final var sut = mock(LeadingTotalReturnsCalculationServiceImpl.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), requestValidator));


        final var reqDTO = mock(LeadingTotalReturnPeriodsReqDTO.class);
        final var leadingTotalReturnCalculation = mock(LeadingTotalReturnsCalculation.class);

        when(sut.defineCalculationMethod(any())).thenReturn(leadingTotalReturnCalculation);

        doCallRealMethod().when(sut).perform(any());
        //ACT
        sut.perform(reqDTO);

        //VERIFY
        verify(sut).defineCalculationMethod(reqDTO);
    }

    @Test
    void perform_verifyLeadingTotalReturnCalculationCalculate() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(LeadingTotalReturnsReqValidator.class);
        final var sut = mock(LeadingTotalReturnsCalculationServiceImpl.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), requestValidator));

        final var reqDTO = mock(LeadingTotalReturnPeriodsReqDTO.class);
        final var periods = mock(Set.class);
        final var leadingTotalReturnCalculation = mock(LeadingTotalReturnsCalculation.class);

        when(reqDTO.getPeriods()).thenReturn(periods);
        when(sut.defineCalculationMethod(any())).thenReturn(leadingTotalReturnCalculation);

        doCallRealMethod().when(sut).perform(any());
        //ACT
        sut.perform(reqDTO);

        //VERIFY
        verify(leadingTotalReturnCalculation).calculate(periods);

    }

    @Test
    void perform_verifyReqDTOCasting2() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(LeadingTotalReturnsReqValidator.class);
        final var sut = mock(LeadingTotalReturnsCalculationServiceImpl.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), requestValidator));

        final var reqDTO = new LeadingTotalReturnPeriodsReqDTO();
        final var leadingTotalReturnCalculation = mock(LeadingTotalReturnsCalculation.class);

        when(sut.defineCalculationMethod(any())).thenReturn(leadingTotalReturnCalculation);

        doCallRealMethod().when(sut).perform(any());
        //ACT
        sut.perform(reqDTO);

        //VERIFY
        verify(requestValidator).validate(reqDTO);
    }

    @Test
    void defineCalculationMethod_verifyBuildCalculationDto() {
        //SETUP
        final var sut = mock(LeadingTotalReturnsCalculationServiceImpl.class);

        final var reqDTO = mock(LeadingTotalReturnPeriodsReqDTO.class);
        final var input = mock(CalculationDTO.class);

        when(sut.buildCalculationDto(any(), any())).thenReturn(input);

        doCallRealMethod().when(sut).defineCalculationMethod(any());
        //ACT
        sut.defineCalculationMethod(reqDTO);

        //VERIFY
        verify(sut).buildCalculationDto(reqDTO, SCALE_OF_TWO);
    }

    @Test
    void defineCalculationMethod_verifyLeadingTotalReturnCalculationConstructor() {
        //SETUP
        final var defaultPeriods = Set.of("3", "6", "12", "24");
        final LeadingTotalReturnsCalculationServiceImpl sut = Mockito.spy(new LeadingTotalReturnsCalculationServiceImpl(null, defaultPeriods, null));

        final var reqDTO = mock(LeadingTotalReturnPeriodsReqDTO.class);
        final var calculationDTO = new CalculationDTO();
        LocalDate cipsd = LocalDate.now();
        calculationDTO.setCipsd(cipsd);
        calculationDTO.setWeightedAveragePortfolioReturns(mock(TreeMap.class));

        doReturn(calculationDTO).when(sut).buildCalculationDto(any(), any());

        doCallRealMethod().when(sut).defineCalculationMethod(any());
        //ACT
        final var actual = sut.defineCalculationMethod(reqDTO);

        //VERIFY
        ComparisonUtils.compareCollections(defaultPeriods, actual.getDefaultPeriods());
        assertEquals(calculationDTO.getCipsd(), actual.getCipsd());
        assertEquals(calculationDTO.getWeightedAveragePortfolioReturns(), actual.getPortfolioTotalReturns());
    }

    @Test
    void buildCalculationDto_verifyGetPortfolioMonthlyReturns() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var sut = mock(LeadingTotalReturnsCalculationServiceImpl.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), mock(LeadingTotalReturnsReqValidator.class)));

        final var reqDTO = mock(LeadingTotalReturnPeriodsReqDTO.class);
        final var holdings = mock(List.class);
        final var currency = Currency.CAD;
        when(reqDTO.getHoldings()).thenReturn(holdings);
        when(reqDTO.getCurrency()).thenReturn(currency);

        final var monthlyReturns = mock(Returns.class, RETURNS_SELF);

        when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);
        doCallRealMethod().when(sut).buildCalculationDto(any(), any());

        //ACT
        sut.buildCalculationDto(reqDTO, SCALE_OF_TWO);

        //VERIFY
        verify(monthlyReturnsService).getPortfolioMonthlyReturns(holdings, Currency.CAD, SCALE_OF_TWO);
    }

    @Test
    void buildCalculationDto_verifyGetWeightedAverage() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var sut = mock(LeadingTotalReturnsCalculationServiceImpl.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), mock(LeadingTotalReturnsReqValidator.class)));

        final var reqDTO = mock(LeadingTotalReturnPeriodsReqDTO.class);
        when(reqDTO.getCustomPsd()).thenReturn(LOCAL_DATE_NOW);

        final var monthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);

        when(monthlyReturns
                .validateCpsd(reqDTO.getCustomPsd())
                .validateReturns()
                .cutByPed()
                .cutByCpsdIfCpsdEmptyCutByPsd(reqDTO.getCustomPsd())
                .fxRatesApplied()
                .getWeightedAverage()).thenReturn(mock(TreeMap.class));

        when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);
        doCallRealMethod().when(sut).buildCalculationDto(any(), any());

        //ACT
        sut.buildCalculationDto(reqDTO, SCALE_OF_TWO);
    }

    @Test
    void buildCalculationDto_checkResult() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var sut = mock(LeadingTotalReturnsCalculationServiceImpl.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), mock(LeadingTotalReturnsReqValidator.class)));

        final var reqDTO = mock(LeadingTotalReturnPeriodsReqDTO.class);
        when(reqDTO.getCustomPsd()).thenReturn(LOCAL_DATE_NOW);

        final var monthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);
        final var portfolioBaseTotalReturn = mock(NavigableMap.class);
        when(monthlyReturns
                .validateCpsd(reqDTO.getCustomPsd())
                .cutByPed()
                .cutByCpsdIfCpsdEmptyCutByPsd(reqDTO.getCustomPsd())
                .fxRatesApplied()
                .getWeightedAverage()).thenReturn(portfolioBaseTotalReturn);

        when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);
        doCallRealMethod().when(sut).buildCalculationDto(any(), any());

        //ACT
        final CalculationDTO actual = sut.buildCalculationDto(reqDTO, SCALE_OF_TWO);

        //VERIFY
        final var expected = new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioBaseTotalReturn);
        assertEquals(actual, expected);

    }

}