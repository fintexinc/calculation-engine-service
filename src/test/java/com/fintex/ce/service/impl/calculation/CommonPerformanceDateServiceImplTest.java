package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.CommonDatesResDTO;
import com.fintex.ce.dto.exception.ErrorRes2DTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.MultiplePortfoliosReqDTO;
import com.fintex.ce.dto.response.CommonPerformanceDatesResDTO;
import com.fintex.ce.exception.DataErrorException;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.util.validation.request.CommonDatesRequestValidator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CommonPerformanceDateServiceImplTest {

    @Test
    void commonPerformanceDateFor_holdingsIsEmpty() {
        //SETUP
        final var sut = mock(CommonPerformanceDateServiceImpl.class);
        final var expected = new CommonDatesResDTO();

        final List holdings = List.of();
        doCallRealMethod().when(sut).getPortfolioMonthlyReturns(anyList());
        doCallRealMethod().when(sut).commonPerformanceDateFor(any());

        final Returns<RMonthlyReturns> monthlyReturns = sut.getPortfolioMonthlyReturns(holdings);

        //ACT
        final CommonDatesResDTO actual = sut.commonPerformanceDateFor(monthlyReturns);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void commonPerformanceDate_verifyValidate() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var commonDatesRequestValidator = mock(CommonDatesRequestValidator.class);
        final var sut = mock(CommonPerformanceDateServiceImpl.class,
                withSettings().useConstructor(monthlyReturnsService, commonDatesRequestValidator));

        final MultiplePortfoliosReqDTO request = mock(MultiplePortfoliosReqDTO.class);
        final List benchmarkHoldings = mock(List.class);
        final Set portfolios = mock(Set.class);

        doReturn(benchmarkHoldings).when(request).getBenchmarkHoldings();
        doReturn(portfolios).when(request).getPortfolios();
        doReturn(mock(CommonDatesResDTO.class)).when(sut).commonPerformanceDateFor(any());

        doCallRealMethod().when(sut).commonPerformanceDate(any());
        //ACT
        sut.commonPerformanceDate(request);

        //VERIFY
        verify(commonDatesRequestValidator).validate(benchmarkHoldings, portfolios);
    }

    @Test
    void collectAllPortfolioHoldings_checkResultIsEmpty_whenPortfolioIsEmpty() {
        //SETUP
        final var sut = mock(CommonPerformanceDateServiceImpl.class);

        doCallRealMethod().when(sut).collectAllPortfolioHoldings(anySet());

        //ACT
        final List<Holding> actual = sut.collectAllPortfolioHoldings(Set.of());

        //VERIFY
        assertTrue(actual.isEmpty());
    }

    @Test
    void collectAllPortfolioHoldings_checkResult() {
        //SETUP
        final var sut = mock(CommonPerformanceDateServiceImpl.class);
        final var portfolio1 = mock(MultiplePortfoliosReqDTO.Portfolio.class);
        final var portfolio2 = mock(MultiplePortfoliosReqDTO.Portfolio.class);

        final var holding1 = mock(Holding.class);
        final var holding2 = mock(Holding.class);

        final var holdings1 = List.of(holding1);
        final var holdings2 = List.of(holding2);

        when(portfolio1.getHoldings()).thenReturn(holdings1);
        when(portfolio2.getHoldings()).thenReturn(holdings2);

        doCallRealMethod().when(sut).collectAllPortfolioHoldings(anySet());

        //ACT
        final List<Holding> actual = sut.collectAllPortfolioHoldings(Set.of(portfolio1, portfolio2));

        //VERIFY
        assertEquals(2, actual.size());
        assertTrue(List.of(holding1, holding2).containsAll(actual));
    }

    @Test
    void commonPerformanceDate_errorResponse() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var commonDatesRequestValidator = mock(CommonDatesRequestValidator.class);
        final var sut = mock(CommonPerformanceDateServiceImpl.class,
                withSettings().useConstructor(monthlyReturnsService, commonDatesRequestValidator));
        final MultiplePortfoliosReqDTO request = mock(MultiplePortfoliosReqDTO.class);
        final Set portfolios = mock(Set.class);
        final DataErrorException error = new DataErrorException("message", "id", ExceptionCode.ERR_RRC_MR_002);
        final ErrorRes2DTO resError = new ErrorRes2DTO("id", ExceptionCode.ERR_RRC_MR_002.toString(), "message");
        final List<DataErrorException> errors = List.of(error);
        final Returns<RMonthlyReturns> returns = mock(Returns.class);

        doReturn(portfolios).when(request).getPortfolios();
        doReturn(mock(CommonDatesResDTO.class)).when(sut).commonPerformanceDateFor(any());
        doReturn(returns).when(sut).getPortfolioMonthlyReturns(any());
        doReturn(errors).when(returns).getErrors();
        doCallRealMethod().when(sut).commonPerformanceDate(any());

        //ACT
        CommonPerformanceDatesResDTO actual = sut.commonPerformanceDate(request);

        //VERIFY
        assertEquals(List.of(resError), actual.getErrors());

    }

    @Test
    void commonPerformanceDateFor_emptyMonthlyReturns() {
        // SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var commonDatesRequestValidator = mock(CommonDatesRequestValidator.class);
        final var sut = mock(CommonPerformanceDateServiceImpl.class,
                withSettings().useConstructor(monthlyReturnsService, commonDatesRequestValidator));
        final var returns = new Returns<RMonthlyReturns>();
        doCallRealMethod().when(sut).commonPerformanceDateFor(any());

        // ACT
        CommonDatesResDTO commonDatesResDTO = sut.commonPerformanceDateFor(returns);

        // VERIFY
        assertNotNull(commonDatesResDTO);
        assertNull(commonDatesResDTO.getEndDate());
        assertNull(commonDatesResDTO.getStartDate());
    }

    private CommonDatesResDTO getCommonDatesForBenchmarkHoldings() {
        return new CommonDatesResDTO()
                .setEndDate(LocalDate.of(2020, 10, 31))
                .setStartDate(LocalDate.of(2020, 5, 31));
    }

    private CommonDatesResDTO getCommonDatesForPortfolioHoldings() {
        return new CommonDatesResDTO()
                .setEndDate(LocalDate.of(2020, 8, 31))
                .setStartDate(LocalDate.of(2020, 4, 30));
    }

    private CommonPerformanceDatesResDTO getExpected(CommonDatesResDTO commonDatesForBenchmarkHoldings, CommonDatesResDTO commonDatesForPortfolioHoldings) {
        return new CommonPerformanceDatesResDTO()
                .setCommonPerformanceEndDatePf(commonDatesForPortfolioHoldings.getEndDate())
                .setCommonPerformanceStartDatePf(commonDatesForPortfolioHoldings.getStartDate())
                .setCommonPerformanceEndDateBm(commonDatesForBenchmarkHoldings.getEndDate())
                .setCommonPerformanceStartDateBm(commonDatesForBenchmarkHoldings.getStartDate());
    }


}