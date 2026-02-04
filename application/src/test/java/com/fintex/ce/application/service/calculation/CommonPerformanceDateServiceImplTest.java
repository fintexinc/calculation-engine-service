package com.fintex.ce.application.service.calculation;

import com.fintex.ce.domain.enumeration.ExceptionCode;
import com.fintex.ce.domain.model.ValidationError;
import com.fintex.ce.monthlyreturns.Returns;
import com.fintex.ce.domain.model.CommonDates;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.MultiplePortfoliosCommand;
import com.fintex.ce.port.input.result.CommonPerformanceDatesResult;
import com.fintex.ce.domain.exception.DataErrorException;
import com.fintex.ce.domain.model.MonthlyReturns;
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
    // SETUP
    final var sut = mock(CommonPerformanceDateServiceImpl.class);
    final var expected = new CommonDates();

    final List holdings = List.of();
    doCallRealMethod().when(sut).getPortfolioMonthlyReturns(anyList());
    doCallRealMethod().when(sut).commonPerformanceDateFor(any());

    final Returns<MonthlyReturns> monthlyReturns = sut.getPortfolioMonthlyReturns(holdings);

    // ACT
    final CommonDates actual = sut.commonPerformanceDateFor(monthlyReturns);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void commonPerformanceDate_verifyValidate() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(CommonPerformanceDateServiceImpl.class,
        withSettings().useConstructor(monthlyReturnsService));

    final MultiplePortfoliosCommand request = mock(MultiplePortfoliosCommand.class);
    final List benchmarkHoldings = mock(List.class);
    final Set portfolios = mock(Set.class);

    doReturn(benchmarkHoldings).when(request).getBenchmarkHoldings();
    doReturn(portfolios).when(request).getPortfolios();
    doReturn(mock(CommonDates.class)).when(sut).commonPerformanceDateFor(any());

    doCallRealMethod().when(sut).commonPerformanceDate(any());
    // ACT
    sut.commonPerformanceDate(request);

    // VERIFY
  }

  @Test
  void collectAllPortfolioHoldings_checkResultIsEmpty_whenPortfolioIsEmpty() {
    // SETUP
    final var sut = mock(CommonPerformanceDateServiceImpl.class);

    doCallRealMethod().when(sut).collectAllPortfolioHoldings(anySet());

    // ACT
    final List<Holding> actual = sut.collectAllPortfolioHoldings(Set.of());

    // VERIFY
    assertTrue(actual.isEmpty());
  }

  @Test
  void collectAllPortfolioHoldings_checkResult() {
    // SETUP
    final var sut = mock(CommonPerformanceDateServiceImpl.class);
    final var portfolio1 = mock(MultiplePortfoliosCommand.Portfolio.class);
    final var portfolio2 = mock(MultiplePortfoliosCommand.Portfolio.class);

    final var holding1 = mock(Holding.class);
    final var holding2 = mock(Holding.class);

    final var holdings1 = List.of(holding1);
    final var holdings2 = List.of(holding2);

    when(portfolio1.getHoldings()).thenReturn(holdings1);
    when(portfolio2.getHoldings()).thenReturn(holdings2);

    doCallRealMethod().when(sut).collectAllPortfolioHoldings(anySet());

    // ACT
    final List<Holding> actual = sut.collectAllPortfolioHoldings(Set.of(portfolio1, portfolio2));

    // VERIFY
    assertEquals(2, actual.size());
    assertTrue(List.of(holding1, holding2).containsAll(actual));
  }

  @Test
  void commonPerformanceDate_errorResponse() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(CommonPerformanceDateServiceImpl.class,
        withSettings().useConstructor(monthlyReturnsService));
    final MultiplePortfoliosCommand request = mock(MultiplePortfoliosCommand.class);
    final Set portfolios = mock(Set.class);
    final DataErrorException error = new DataErrorException("message", "id", ExceptionCode.ERR_RRC_MR_002);
    final ValidationError resError = new ValidationError("id", ExceptionCode.ERR_RRC_MR_002.toString(), "message");
    final List<DataErrorException> errors = List.of(error);
    final Returns<MonthlyReturns> returns = mock(Returns.class);

    doReturn(portfolios).when(request).getPortfolios();
    doReturn(mock(CommonDates.class)).when(sut).commonPerformanceDateFor(any());
    doReturn(returns).when(sut).getPortfolioMonthlyReturns(any());
    doReturn(errors).when(returns).getErrors();
    doCallRealMethod().when(sut).commonPerformanceDate(any());

    // ACT
    CommonPerformanceDatesResult actual = sut.commonPerformanceDate(request);

    // VERIFY
    assertEquals(List.of(resError), actual.getErrors());

  }

  @Test
  void commonPerformanceDateFor_emptyMonthlyReturns() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(CommonPerformanceDateServiceImpl.class,
        withSettings().useConstructor(monthlyReturnsService));
    final var returns = new Returns<MonthlyReturns>();
    doCallRealMethod().when(sut).commonPerformanceDateFor(any());

    // ACT
    CommonDates commonDates = sut.commonPerformanceDateFor(returns);

    // VERIFY
    assertNotNull(commonDates);
    assertNull(commonDates.getEnd());
    assertNull(commonDates.getStart());
  }

  private CommonDates getCommonDatesForBenchmarkHoldings() {
    return new CommonDates()
        .setEnd(LocalDate.of(2020, 10, 31))
        .setStart(LocalDate.of(2020, 5, 31));
  }

  private CommonDates getCommonDatesForPortfolioHoldings() {
    return new CommonDates()
        .setEnd(LocalDate.of(2020, 8, 31))
        .setStart(LocalDate.of(2020, 4, 30));
  }

  private CommonPerformanceDatesResult getExpected(CommonDates commonDatesForBenchmarkHoldings,
      CommonDates commonDatesForPortfolioHoldings) {
    return new CommonPerformanceDatesResult()
        .setCommonPerformanceEndDatePf(commonDatesForPortfolioHoldings.getEnd())
        .setCommonPerformanceStartDatePf(commonDatesForPortfolioHoldings.getStart())
        .setCommonPerformanceEndDateBm(commonDatesForBenchmarkHoldings.getEnd())
        .setCommonPerformanceStartDateBm(commonDatesForBenchmarkHoldings.getStart());
  }

}