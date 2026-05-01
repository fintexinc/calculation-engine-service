package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.MonthlyReturnsGenerator;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.returns.ReturnsCutComponent;
import com.fintex.ce.application.returns.WeightedAverageComponent;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.application.validation.BenchmarkCpedDataValidation;
import com.fintex.ce.application.validation.BenchmarkCpsdDataValidation;
import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.exceptions.BasePceException;
import com.fintex.ce.port.webclient.boc.FxRatesFetcher;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.application.util.TestConstants.LOCAL_DATE_NOW;
import static com.fintex.ce.model.error.ErrorCode.FX_RATES_UNAVAILABLE;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static com.fintex.wm.commons.domain.currency.Currency.CAD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class MonthlyReturnsServiceTest {

  @Test
  void shouldGetWeightedAverageWithCpsdAndCpedValidation_whenVerifyGetWeightedAverage() {
    var service = mock(MonthlyReturnsService.class);
    var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    var portfolioBaseTotalReturns = mock(TreeMap.class);

    when(monthlyReturns
        .validateCped(eq(LOCAL_DATE_NOW.plusMonths(3)))
        .validateCpsd(eq(LOCAL_DATE_NOW))
        .validateReturns()
        .cutByCpedIfCpedEmptyCutByPed(eq(LOCAL_DATE_NOW.plusMonths(3)))
        .cutByCpsdIfCpsdEmptyCutByPsd(eq(LOCAL_DATE_NOW))
        .fxRatesApplied()
        .getWeightedAverage()).thenReturn(portfolioBaseTotalReturns);

    doCallRealMethod().when(service).getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any());

    service.getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, LOCAL_DATE_NOW, LOCAL_DATE_NOW.plusMonths(3));
  }

  @Test
  void shouldGetWeightedAverageWithCpsdAndCpedValidation_whenCheckResult() {
    var service = mock(MonthlyReturnsService.class);
    var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    var portfolioBaseTotalReturns = mock(TreeMap.class);

    when(monthlyReturns
        .validateCped(eq(LOCAL_DATE_NOW.plusMonths(3)))
        .validateCpsd(eq(LOCAL_DATE_NOW))
        .cutByCpedIfCpedEmptyCutByPed(eq(LOCAL_DATE_NOW.plusMonths(3)))
        .cutByCpsdIfCpsdEmptyCutByPsd(eq(LOCAL_DATE_NOW))
        .fxRatesApplied()
        .getWeightedAverage()).thenReturn(portfolioBaseTotalReturns);

    doCallRealMethod().when(service).getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any());

    NavigableMap<LocalDate, BigDecimal> actual = service.getWeightedAverageWithCpsdAndCpedValidation(
        monthlyReturns, LOCAL_DATE_NOW, LOCAL_DATE_NOW.plusMonths(3));

    assertSame(portfolioBaseTotalReturns, actual);
  }

  @Test
  void shouldGetWeightedAverageWithCpedValidation_whenVerifyGetWeightedAverage() {
    var service = mock(MonthlyReturnsService.class);
    var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    var portfolioBaseTotalReturns = mock(TreeMap.class);

    when(monthlyReturns
        .validateCped(eq(LOCAL_DATE_NOW))
        .validateReturns()
        .cutByCpedIfCpedEmptyCutByPed(eq(LOCAL_DATE_NOW))
        .cutByPsd()
        .fxRatesApplied()
        .getWeightedAverage()).thenReturn(portfolioBaseTotalReturns);

    doCallRealMethod().when(service).getWeightedAverageWithCpedValidation(any(), any());

    service.getWeightedAverageWithCpedValidation(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldGetWeightedAverageWithCpedValidation_whenCheckResult() {
    var service = mock(MonthlyReturnsService.class);
    var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    var portfolioBaseTotalReturns = mock(TreeMap.class);

    when(monthlyReturns
        .validateCped(eq(LOCAL_DATE_NOW))
        .cutByCpedIfCpedEmptyCutByPed(eq(LOCAL_DATE_NOW))
        .cutByPsd()
        .fxRatesApplied()
        .getWeightedAverage()).thenReturn(portfolioBaseTotalReturns);

    doCallRealMethod().when(service).getWeightedAverageWithCpedValidation(any(), any());

    NavigableMap<LocalDate, BigDecimal> actual = service.getWeightedAverageWithCpedValidation(monthlyReturns,
        LOCAL_DATE_NOW);

    assertSame(portfolioBaseTotalReturns, actual);
  }

  @Test
  void shouldGetMonthlyReturns_whenVerifyLoad() {
    try (MockedConstruction<ReturnsAggregate> mocked = Mockito.mockConstruction(ReturnsAggregate.class)) {
      var monthlyReturnsFetcher = mock(SecurityDataFetcher.class);
      var gicMonthlyReturnsGenerator = mock(MonthlyReturnsGenerator.class);
      var service = mock(MonthlyReturnsService.class, withSettings()
          .useConstructor(monthlyReturnsFetcher, mock(FxRateService.class), gicMonthlyReturnsGenerator));

      var holdings = mock(List.class);

      when(monthlyReturnsFetcher.fetch(any(), any())).thenReturn(new HashMap<>());
      doCallRealMethod().when(service).getMonthlyReturns(anyList(), any());

      service.getMonthlyReturns(holdings, CAD);

      verify(monthlyReturnsFetcher).fetch(holdings, List.of());
    }
  }

  @Test
  void shouldGetMonthlyReturns_whenCheckResult() {
    var monthlyReturnsFetcher = mock(SecurityDataFetcher.class);
    var gicMonthlyReturnsGenerator = mock(MonthlyReturnsGenerator.class);
    var service = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(monthlyReturnsFetcher, mock(FxRateService.class), gicMonthlyReturnsGenerator));

    var originalMonthlyReturns = mock(Map.class);
    when(monthlyReturnsFetcher.fetch(any(), any())).thenReturn(originalMonthlyReturns);
    ReturnsAggregate expected = mock(ReturnsAggregate.class);
    when(service.getMonthlyReturns(originalMonthlyReturns)).thenReturn(expected);

    doCallRealMethod().when(service).getMonthlyReturns(anyList(), any());

    var actual = service.getMonthlyReturns(mock(List.class), Currency.CAD);

    assertEquals(expected, actual);
  }

  @Test
  void shouldGetMonthlyReturns_whenVerifyGicWasGenerated() {
    var monthlyReturnsFetcher = mock(SecurityDataFetcher.class);
    var gicMonthlyReturnsGenerator = mock(MonthlyReturnsGenerator.class);
    var service = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(monthlyReturnsFetcher, mock(FxRateService.class), gicMonthlyReturnsGenerator));

    var originalMonthlyReturns = mock(Map.class);
    when(monthlyReturnsFetcher.fetch(any(), any())).thenReturn(originalMonthlyReturns);
    Map gicOriginalMonthlyReturns = mock(Map.class);
    when(gicMonthlyReturnsGenerator.generateGicMonthlyReturns(anyList())).thenReturn(gicOriginalMonthlyReturns);

    doCallRealMethod().when(service).getMonthlyReturns(anyList(), any());

    service.getMonthlyReturns(mock(List.class), Currency.CAD);

    verify(originalMonthlyReturns).putAll(gicOriginalMonthlyReturns);
  }

  @Test
  void shouldGetPortfolioMonthlyReturns_whenVerifyGetMonthlyReturns() {
    var fxRateService = mock(FxRateService.class);
    var service = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(mock(SecurityDataFetcher.class), fxRateService, mock(MonthlyReturnsGenerator.class)));

    var holdings = mock(List.class);
    var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    monthlyReturns.holdingCurrencyMap = new HashMap<>();
    when(service.getMonthlyReturns(anyList(), any())).thenReturn(monthlyReturns);

    doCallRealMethod().when(service).getPortfolioMonthlyReturns(anyList(), any(), any());

    service.getPortfolioMonthlyReturns(holdings, CAD, ReturnFactorScale.SCALE_OF_TWO);

    verify(service).getMonthlyReturns(holdings, CAD);
  }

  @Test
  void shouldGetPortfolioMonthlyReturns_whenVerifyInit() {
    var fxRateService = mock(FxRateService.class);
    var service = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(mock(SecurityDataFetcher.class), fxRateService, mock(MonthlyReturnsGenerator.class)));

    var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_SELF);
    monthlyReturns.holdingCurrencyMap = new HashMap<>();
    when(service.getMonthlyReturns(anyList(), any())).thenReturn(monthlyReturns);

    doCallRealMethod().when(service).getPortfolioMonthlyReturns(anyList(), any(), any());

    service.getPortfolioMonthlyReturns(mock(List.class), CAD, ReturnFactorScale.SCALE_OF_TWO);

    var inOrder = inOrder(monthlyReturns);

    inOrder.verify(monthlyReturns).setFxRateService(any());
    inOrder.verify(monthlyReturns).setMonthlyReturnsCutComponent(eq(new ReturnsCutComponent()));
    inOrder.verify(monthlyReturns).setWeightedAverageComponent(eq(new WeightedAverageComponent(
        ReturnFactorScale.SCALE_OF_TWO)));
    inOrder.verify(monthlyReturns).setCpsdDataValidation(eq(new PortfolioCpsdDataValidation()));
    inOrder.verify(monthlyReturns).setCpedDataValidation(eq(new PortfolioCpedDataValidation()));
  }

  @Test
  void shouldGetPortfolioMonthlyReturns_whenCheckResult() {
    var fxRateService = mock(FxRateService.class);
    var service = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(mock(SecurityDataFetcher.class), fxRateService, mock(MonthlyReturnsGenerator.class)));

    var holdings = mock(List.class);
    var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    monthlyReturns.holdingCurrencyMap = new HashMap<>();
    when(service.getMonthlyReturns(anyList(), any())).thenReturn(monthlyReturns);

    doCallRealMethod().when(service).getPortfolioMonthlyReturns(anyList(), any(), any());

    var actual = service.getPortfolioMonthlyReturns(holdings, CAD, ReturnFactorScale.SCALE_OF_TWO);

    assertSame(monthlyReturns, actual);
  }

  @Test
  void shouldGetBenchmarkMonthlyReturns_whenVerifyGetMonthlyReturns() {
    var fxRateService = mock(FxRateService.class);
    var service = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(mock(SecurityDataFetcher.class), fxRateService, mock(MonthlyReturnsGenerator.class)));

    var holdings = mock(List.class);
    var benchmarkMonthlyReturns = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    benchmarkMonthlyReturns.holdingCurrencyMap = new HashMap<>();
    when(service.getMonthlyReturns(anyList(), any())).thenReturn(benchmarkMonthlyReturns);

    doCallRealMethod().when(service).getBenchmarkMonthlyReturns(anyList(), any(), any());

    service.getBenchmarkMonthlyReturns(holdings, CAD, ReturnFactorScale.SCALE_OF_TWO);

    verify(service).getMonthlyReturns(holdings, CAD);
  }

  @Test
  void shouldGetBenchmarkMonthlyReturns_whenVerifyInit() {
    var fxRateService = mock(FxRateService.class);
    var service = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(mock(SecurityDataFetcher.class), fxRateService, mock(MonthlyReturnsGenerator.class)));

    var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_SELF);
    monthlyReturns.holdingCurrencyMap = new HashMap<>();
    when(service.getMonthlyReturns(anyList(), any())).thenReturn(monthlyReturns);

    doCallRealMethod().when(service).getBenchmarkMonthlyReturns(anyList(), any(), any());

    service.getBenchmarkMonthlyReturns(mock(List.class), CAD, ReturnFactorScale.SCALE_OF_TWO);

    var inOrder = inOrder(monthlyReturns);

    inOrder.verify(monthlyReturns).setFxRateService(any());
    inOrder.verify(monthlyReturns).setMonthlyReturnsCutComponent(eq(new ReturnsCutComponent()));
    inOrder.verify(monthlyReturns).setWeightedAverageComponent(eq(new WeightedAverageComponent(
        ReturnFactorScale.SCALE_OF_TWO)));
    inOrder.verify(monthlyReturns).setCpsdDataValidation(eq(new BenchmarkCpsdDataValidation()));
    inOrder.verify(monthlyReturns).setCpedDataValidation(eq(new BenchmarkCpedDataValidation()));
  }

  @Test
  void shouldKeepOriginalReturnsAndAddWarning_whenFxRatesUnavailable() {
    var monthlyReturnsFetcher = mock(SecurityDataFetcher.class);
    var fxRatesFetcher = mock(FxRatesFetcher.class);
    var fxRateService = new FxRateService(fxRatesFetcher);
    var gicMonthlyReturnsGenerator = mock(MonthlyReturnsGenerator.class);
    var service = new MonthlyReturnsService(monthlyReturnsFetcher, fxRateService, gicMonthlyReturnsGenerator);

    PortfolioHolding holding = new PortfolioHolding(BigDecimal.valueOf(1000),
        FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("Ticker", FiIdentifierType.TICKER));
    TreeMap<LocalDate, BigDecimal> originalReturns = new TreeMap<>();
    originalReturns.put(toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(2)), BigDecimal.valueOf(0.01));
    originalReturns.put(toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), BigDecimal.valueOf(0.02));

    HoldingMonthlyReturns monthly = new HoldingMonthlyReturns();
    monthly.setCurrency(Currency.USD.name());
    monthly.setHoldingType(FinancialInstrumentType.MUTUAL_FUND_CANADA);
    monthly.setReturns(new TreeMap<>(originalReturns));

    HashMap<PortfolioHolding, HoldingMonthlyReturns> fetched = new HashMap<>();
    fetched.put(holding, monthly);

    when(monthlyReturnsFetcher.fetch(anyList(), anyList())).thenReturn(fetched);
    when(gicMonthlyReturnsGenerator.generateGicMonthlyReturns(anyList())).thenReturn(new HashMap<>());
    when(fxRatesFetcher.fetch(any(CurrencyExchangePair.class), any(DateRange.class))).thenReturn(new TreeMap<>());

    ReturnsAggregate<HoldingMonthlyReturns> aggregate = service.getPortfolioMonthlyReturns(
        List.of(holding), CAD, ReturnFactorScale.SCALE_OF_TWO);
    aggregate.fxRatesApplied();

    assertEquals(originalReturns, aggregate.returnsMap.get(holding));
    assertEquals(Currency.USD, aggregate.holdingCurrencyMap.get(holding));
    List<BasePceException> exceptions = aggregate.notification.getExceptions();
    assertEquals(1, exceptions.size());
    assertEquals(FX_RATES_UNAVAILABLE, exceptions.getFirst().getErrorCode());
  }

  @Test
  void shouldKeepAllReturnsUnconvertedAndAddWarning_whenRatesArePartiallyAvailable() {
    var monthlyReturnsFetcher = mock(SecurityDataFetcher.class);
    var fxRatesFetcher = mock(FxRatesFetcher.class);
    var fxRateService = new FxRateService(fxRatesFetcher);
    var gicMonthlyReturnsGenerator = mock(MonthlyReturnsGenerator.class);
    var service = new MonthlyReturnsService(monthlyReturnsFetcher, fxRateService, gicMonthlyReturnsGenerator);

    PortfolioHolding holding = new PortfolioHolding(BigDecimal.valueOf(1000),
        FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("Ticker", FiIdentifierType.TICKER));
    LocalDate firstMonth = toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(3));
    LocalDate secondMonth = toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(2));
    LocalDate thirdMonth = toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1));
    TreeMap<LocalDate, BigDecimal> originalReturns = new TreeMap<>();
    originalReturns.put(firstMonth, BigDecimal.valueOf(0.01));
    originalReturns.put(secondMonth, BigDecimal.valueOf(0.02));
    originalReturns.put(thirdMonth, BigDecimal.valueOf(0.03));

    HoldingMonthlyReturns monthly = new HoldingMonthlyReturns();
    monthly.setCurrency(Currency.USD.name());
    monthly.setHoldingType(FinancialInstrumentType.MUTUAL_FUND_CANADA);
    monthly.setReturns(new TreeMap<>(originalReturns));

    HashMap<PortfolioHolding, HoldingMonthlyReturns> fetched = new HashMap<>();
    fetched.put(holding, monthly);

    TreeMap<LocalDate, BigDecimal> partialRates = new TreeMap<>();
    partialRates.put(thirdMonth, BigDecimal.valueOf(1.35));

    when(monthlyReturnsFetcher.fetch(anyList(), anyList())).thenReturn(fetched);
    when(gicMonthlyReturnsGenerator.generateGicMonthlyReturns(anyList())).thenReturn(new HashMap<>());
    when(fxRatesFetcher.fetch(any(CurrencyExchangePair.class), any(DateRange.class))).thenReturn(partialRates);

    ReturnsAggregate<HoldingMonthlyReturns> aggregate = service.getPortfolioMonthlyReturns(
        List.of(holding), CAD, ReturnFactorScale.SCALE_OF_TWO);
    aggregate.fxRatesApplied();

    assertEquals(originalReturns, aggregate.returnsMap.get(holding));
    assertEquals(Currency.USD, aggregate.holdingCurrencyMap.get(holding));
    List<BasePceException> exceptions = aggregate.notification.getExceptions();
    assertEquals(1, exceptions.size());
    assertEquals(FX_RATES_UNAVAILABLE, exceptions.getFirst().getErrorCode());
  }

  @Test
  void shouldUpdateCurrencyToTarget_whenConversionSucceeds() {
    var monthlyReturnsFetcher = mock(SecurityDataFetcher.class);
    var fxRatesFetcher = mock(FxRatesFetcher.class);
    var fxRateService = new FxRateService(fxRatesFetcher);
    var gicMonthlyReturnsGenerator = mock(MonthlyReturnsGenerator.class);
    var service = new MonthlyReturnsService(monthlyReturnsFetcher, fxRateService, gicMonthlyReturnsGenerator);

    PortfolioHolding holding = new PortfolioHolding(BigDecimal.valueOf(1000),
        FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("Ticker", FiIdentifierType.TICKER));
    LocalDate firstMonth = toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(3));
    LocalDate secondMonth = toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(2));
    LocalDate thirdMonth = toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1));
    TreeMap<LocalDate, BigDecimal> originalReturns = new TreeMap<>();
    originalReturns.put(firstMonth, BigDecimal.valueOf(0.01));
    originalReturns.put(secondMonth, BigDecimal.valueOf(0.02));
    originalReturns.put(thirdMonth, BigDecimal.valueOf(0.03));

    HoldingMonthlyReturns monthly = new HoldingMonthlyReturns();
    monthly.setCurrency(Currency.USD.name());
    monthly.setHoldingType(FinancialInstrumentType.MUTUAL_FUND_CANADA);
    monthly.setReturns(new TreeMap<>(originalReturns));

    HashMap<PortfolioHolding, HoldingMonthlyReturns> fetched = new HashMap<>();
    fetched.put(holding, monthly);

    TreeMap<LocalDate, BigDecimal> fullRates = new TreeMap<>();
    fullRates.put(toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(4)), BigDecimal.valueOf(1.30));
    fullRates.put(firstMonth, BigDecimal.valueOf(1.31));
    fullRates.put(secondMonth, BigDecimal.valueOf(1.32));
    fullRates.put(thirdMonth, BigDecimal.valueOf(1.33));

    when(monthlyReturnsFetcher.fetch(anyList(), anyList())).thenReturn(fetched);
    when(gicMonthlyReturnsGenerator.generateGicMonthlyReturns(anyList())).thenReturn(new HashMap<>());
    when(fxRatesFetcher.fetch(any(CurrencyExchangePair.class), any(DateRange.class))).thenReturn(fullRates);

    ReturnsAggregate<HoldingMonthlyReturns> aggregate = service.getPortfolioMonthlyReturns(
        List.of(holding), CAD, ReturnFactorScale.SCALE_OF_TWO);
    aggregate.fxRatesApplied();

    assertNotEquals(originalReturns, aggregate.returnsMap.get(holding));
    assertEquals(CAD, aggregate.holdingCurrencyMap.get(holding));
    assertTrue(aggregate.notification.getExceptions().isEmpty());
  }

}
