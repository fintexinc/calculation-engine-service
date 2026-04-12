package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.Returns;
import com.fintex.ce.domain.dto.command.MultiplePortfoliosCommand;
import com.fintex.ce.domain.exception.DataErrorException;
import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.domain.model.CommonDates;
import com.fintex.ce.domain.model.HoldingMonthlyReturns;
import com.fintex.ce.domain.model.ValidationError;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.CommonPerformanceDatesResult;

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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CommonPerformanceDateServiceImplTest {

  @Test
  void shouldCommonPerformanceDateFor_whenHoldingsIsEmpty() {
    // SETUP
    final var sut = mock(CommonPerformanceDateServiceImpl.class);
    final var expected = new CommonDates();

    final List holdings = List.of();
    doCallRealMethod().when(sut).getPortfolioMonthlyReturns(anyList());
    doCallRealMethod().when(sut).commonPerformanceDateFor(any());

    final Returns<HoldingMonthlyReturns> monthlyReturns = sut.getPortfolioMonthlyReturns(holdings);

    // ACT
    final CommonDates actual = sut.commonPerformanceDateFor(monthlyReturns);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldCommonPerformanceDate_whenVerifyValidate() {
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

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(request);

    // VERIFY
  }

  @Test
  void shouldCollectAllPortfolioHoldings_whenCheckResultIsEmptyWhenPortfolioIsEmpty() {
    // SETUP
    final var sut = mock(CommonPerformanceDateServiceImpl.class);

    doCallRealMethod().when(sut).collectAllPortfolioHoldings(anySet());

    // ACT
    final List<Holding> actual = sut.collectAllPortfolioHoldings(Set.of());

    // VERIFY
    assertTrue(actual.isEmpty());
  }

  @Test
  void shouldCollectAllPortfolioHoldings_whenCheckResult() {
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
  void shouldCommonPerformanceDate_whenErrorResponse() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(CommonPerformanceDateServiceImpl.class,
        withSettings().useConstructor(monthlyReturnsService));
    final MultiplePortfoliosCommand request = mock(MultiplePortfoliosCommand.class);
    final Set portfolios = mock(Set.class);
    final DataErrorException error = new DataErrorException("message", "id", ErrorCode.ERR_RRC_MR_002);
    final ValidationError resError = new ValidationError("id", ErrorCode.ERR_RRC_MR_002.toString(), "message");
    final List<DataErrorException> errors = List.of(error);
    final Returns<HoldingMonthlyReturns> returns = mock(Returns.class);

    doReturn(portfolios).when(request).getPortfolios();
    doReturn(mock(CommonDates.class)).when(sut).commonPerformanceDateFor(any());
    doReturn(returns).when(sut).getPortfolioMonthlyReturns(any());
    doReturn(errors).when(returns).getErrors();
    doCallRealMethod().when(sut).perform(any());

    // ACT
    CommonPerformanceDatesResult actual = sut.perform(request);

    // VERIFY
    assertEquals(List.of(resError), actual.getErrors());

  }

  @Test
  void shouldCommonPerformanceDateFor_whenEmptyMonthlyReturns() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(CommonPerformanceDateServiceImpl.class,
        withSettings().useConstructor(monthlyReturnsService));
    final var returns = new Returns<HoldingMonthlyReturns>();
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