package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.CommonPerformanceDatesResult;
import com.fintex.ce.model.dto.command.MultiplePortfoliosCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.ValidationError;
import com.fintex.ce.model.error.exceptions.DataErrorException;

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
    final var sut = mock(CommonPerformanceDateServiceImpl.class);
    final var expected = DateRange.UNBOUNDED;

    final List holdings = List.of();
    doCallRealMethod().when(sut).getPortfolioMonthlyReturns(anyList());
    doCallRealMethod().when(sut).commonPerformanceDateFor(any());

    final ReturnsAggregate<HoldingMonthlyReturns> monthlyReturnsAggregate = sut.getPortfolioMonthlyReturns(holdings);

    final DateRange actual = sut.commonPerformanceDateFor(monthlyReturnsAggregate);

    assertEquals(expected, actual);
  }

  @Test
  void shouldCommonPerformanceDate_whenVerifyValidate() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(CommonPerformanceDateServiceImpl.class,
        withSettings().useConstructor(monthlyReturnsService));

    final MultiplePortfoliosCommand request = mock(MultiplePortfoliosCommand.class);
    final List benchmarkHoldings = mock(List.class);
    final Set portfolios = mock(Set.class);

    doReturn(benchmarkHoldings).when(request).getBenchmarkHoldings();
    doReturn(portfolios).when(request).getPortfolios();
    doReturn(DateRange.UNBOUNDED).when(sut).commonPerformanceDateFor(any());

    doCallRealMethod().when(sut).perform(any());
    sut.perform(request);
  }

  @Test
  void shouldCollectAllPortfolioHoldings_whenCheckResultIsEmptyWhenPortfolioIsEmpty() {
    final var sut = mock(CommonPerformanceDateServiceImpl.class);

    doCallRealMethod().when(sut).collectAllPortfolioHoldings(anySet());

    final List<PortfolioHolding> actual = sut.collectAllPortfolioHoldings(Set.of());

    assertTrue(actual.isEmpty());
  }

  @Test
  void shouldCollectAllPortfolioHoldings_whenCheckResult() {
    final var sut = mock(CommonPerformanceDateServiceImpl.class);
    final var portfolio1 = mock(MultiplePortfoliosCommand.Portfolio.class);
    final var portfolio2 = mock(MultiplePortfoliosCommand.Portfolio.class);

    final var holding1 = mock(PortfolioHolding.class);
    final var holding2 = mock(PortfolioHolding.class);

    final var holdings1 = List.of(holding1);
    final var holdings2 = List.of(holding2);

    when(portfolio1.getHoldings()).thenReturn(holdings1);
    when(portfolio2.getHoldings()).thenReturn(holdings2);

    doCallRealMethod().when(sut).collectAllPortfolioHoldings(anySet());

    final List<PortfolioHolding> actual = sut.collectAllPortfolioHoldings(Set.of(portfolio1, portfolio2));

    assertEquals(2, actual.size());
    assertTrue(List.of(holding1, holding2).containsAll(actual));
  }

  @Test
  void shouldCommonPerformanceDate_whenErrorResponse() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(CommonPerformanceDateServiceImpl.class,
        withSettings().useConstructor(monthlyReturnsService));
    final MultiplePortfoliosCommand request = mock(MultiplePortfoliosCommand.class);
    final Set portfolios = mock(Set.class);
    final DataErrorException error = new DataErrorException("message", "id", ErrorCode.ERR_RRC_MR_002);
    final ValidationError resError = new ValidationError("id", ErrorCode.ERR_RRC_MR_002.toString(), "message");
    final List<DataErrorException> errors = List.of(error);
    final ReturnsAggregate<HoldingMonthlyReturns> returnsAggregate = mock(ReturnsAggregate.class);

    doReturn(portfolios).when(request).getPortfolios();
    doReturn(DateRange.UNBOUNDED).when(sut).commonPerformanceDateFor(any());
    doReturn(returnsAggregate).when(sut).getPortfolioMonthlyReturns(any());
    doReturn(errors).when(returnsAggregate).getErrors();
    doCallRealMethod().when(sut).perform(any());

    CommonPerformanceDatesResult actual = sut.perform(request);

    assertEquals(List.of(resError), actual.getErrors());
  }

  @Test
  void shouldCommonPerformanceDateFor_whenEmptyMonthlyReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(CommonPerformanceDateServiceImpl.class,
        withSettings().useConstructor(monthlyReturnsService));
    final var returns = new ReturnsAggregate<HoldingMonthlyReturns>();
    doCallRealMethod().when(sut).commonPerformanceDateFor(any());

    DateRange dateRange = sut.commonPerformanceDateFor(returns);

    assertNotNull(dateRange);
    assertNull(dateRange.end());
    assertNull(dateRange.start());
  }

  private DateRange getDateRangeForBenchmarkHoldings() {
    return new DateRange(LocalDate.of(2020, 5, 31), LocalDate.of(2020, 10, 31));
  }

  private DateRange getDateRangeForPortfolioHoldings() {
    return new DateRange(LocalDate.of(2020, 4, 30), LocalDate.of(2020, 8, 31));
  }

  private CommonPerformanceDatesResult getExpected(DateRange dateRangeForBenchmarkHoldings,
      DateRange dateRangeForPortfolioHoldings) {
    return new CommonPerformanceDatesResult()
        .setCommonPerformanceEndDatePf(dateRangeForPortfolioHoldings.end())
        .setCommonPerformanceStartDatePf(dateRangeForPortfolioHoldings.start())
        .setCommonPerformanceEndDateBm(dateRangeForBenchmarkHoldings.end())
        .setCommonPerformanceStartDateBm(dateRangeForBenchmarkHoldings.start());
  }

}
