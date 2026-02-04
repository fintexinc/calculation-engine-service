package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.MonthlyReturnsCacheStorage;
import com.fintex.ce.adapter.cache.TBillsCacheStorage;
import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.MonthlyReturns;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.output.ReturnsGenerator;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.FIXED_INCOME_PREDICATE;
import static com.fintex.ce.util.FilterUtils.PAG_GUIDED_PORTFOLIO_PREDICATE;
import static com.fintex.ce.util.FilterUtils.SEPARATELY_MANAGED_ACCOUNT_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class MonthlyReturnsCacheStorageTest {

  @Test
  void load_verifyFilters() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(MonthlyReturnsCacheStorage.class);

      final var holdings = List.of(mock(Holding.class));
      final var cad = Currency.CAD;

      doCallRealMethod().when(sut).load(any(), any(), any(), any());
      // ACT
      sut.load(holdings, List.of(), List.of(), new ParamHolderDTO(cad));

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(BENCHMARKS_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(FIXED_INCOME_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(
          SEPARATELY_MANAGED_ACCOUNT_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(PAG_GUIDED_PORTFOLIO_PREDICATE)));
      verify(sut).addCashReturns(eq(holdings), eq(cad), any());
    }
  }

  @Test
  void addCashReturns_verifyFilterHoldings() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(MonthlyReturnsCacheStorage.class);

      final var holdings = List.of(mock(Holding.class));
      final var cad = Currency.CAD;

      doCallRealMethod().when(sut).addCashReturns(any(), any(), any());
      // ACT
      sut.addCashReturns(holdings, cad, Map.of());

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(holdings, CASH_PREDICATE));
    }
  }

  @Test
  void addCashReturns_verifyLoadTBillsFor() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var tBillsCacheStorage = mock(TBillsCacheStorage.class);
      final var sut = mock(MonthlyReturnsCacheStorage.class,
          withSettings()
              .useConstructor(null, null, null, tBillsCacheStorage, null, mock(ReturnsGenerator.class)));

      final var holdings = List.of(mock(Holding.class));
      final var cad = Currency.CAD;

      final var cashes = List.of(mock(CashHolding.class));
      mockedFilterUtils.when((() -> FilterUtils.filterHoldings(any(), any()))).thenReturn(cashes);

      when(sut.checkCurrency(any(), any())).thenReturn(Currency.CAD.name());

      doCallRealMethod().when(sut).addCashReturns(any(), any(), any());
      // ACT
      sut.addCashReturns(holdings, cad, new HashMap<>());

      // VERIFY
      verify(tBillsCacheStorage).loadTBillsFor(cad);
    }
  }

  @Test
  void addCashReturns_verifyCacheHoldingHasInterestRate() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var tBillsCacheStorage = mock(TBillsCacheStorage.class);
      final var sut = mock(MonthlyReturnsCacheStorage.class,
          withSettings()
              .useConstructor(null, null, null, tBillsCacheStorage, null, mock(ReturnsGenerator.class)));

      final var holdings = List.of(mock(Holding.class));
      final var cad = Currency.CAD;

      final var cashes = List.of(mock(CashHolding.class));
      mockedFilterUtils.when((() -> FilterUtils.filterHoldings(any(), any()))).thenReturn(cashes);

      when(sut.checkCurrency(any(), any())).thenReturn(Currency.CAD.name());

      doCallRealMethod().when(sut).addCashReturns(any(), any(), any());
      // ACT
      sut.addCashReturns(holdings, cad, new HashMap<>());

      // VERIFY
      cashes.forEach(cash -> {
        verify(cash).hasClientIntRate();
      });
    }
  }

  @Test
  void addCashReturns_checkResult() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var tBillsCacheStorage = mock(TBillsCacheStorage.class);
      final var sut = mock(MonthlyReturnsCacheStorage.class,
          withSettings()
              .useConstructor(null, null, null, tBillsCacheStorage, null, mock(ReturnsGenerator.class)));

      final var cashHolding = mock(CashHolding.class);
      when(cashHolding.getType()).thenReturn(HoldingType.CASH);
      when(cashHolding.hasClientIntRate()).thenReturn(false);

      final var cad = Currency.CAD;

      final var cashes = List.of(cashHolding);
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(any(), any())).thenReturn(cashes);

      final var tBills = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.ONE));
      when(tBillsCacheStorage.loadTBillsFor(any())).thenReturn(tBills);

      doCallRealMethod().when(sut).checkCurrency(any(), any());

      doCallRealMethod().when(sut).addCashReturns(any(), any(), any());
      // ACT
      final HashMap<Holding, MonthlyReturns> actual = new HashMap<>();
      sut.addCashReturns(List.of(cashHolding), cad, actual);

      // VERIFY
      Assertions.assertEquals(Map.of(cashHolding, new MonthlyReturns().setCurrency(cad.name()).setHoldingType(
          HoldingType.CASH).setReturns(tBills)), actual);
    }
  }

  @Test
  void addCashReturns_checkResultWhenInterestRateProvided() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var tBillsCacheStorage = mock(TBillsCacheStorage.class);
      var returnsGenerator = mock(ReturnsGenerator.class);
      final var sut = mock(MonthlyReturnsCacheStorage.class,
          withSettings()
              .useConstructor(null, null, null, tBillsCacheStorage, null, returnsGenerator));

      final var cashHolding = mock(CashHolding.class);
      when(cashHolding.getType()).thenReturn(HoldingType.CASH);
      when(cashHolding.hasClientIntRate()).thenReturn(true);

      final var cad = Currency.CAD;

      final var cashes = List.of(cashHolding);
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(any(), any())).thenReturn(cashes);

      final var generatedReturns = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.ONE));
      when(returnsGenerator.generateReturns(cashHolding)).thenReturn(generatedReturns);

      doCallRealMethod().when(sut).checkCurrency(any(), any());

      doCallRealMethod().when(sut).addCashReturns(any(), any(), any());
      // ACT
      final HashMap<Holding, MonthlyReturns> actual = new HashMap<>();
      sut.addCashReturns(List.of(cashHolding), cad, actual);

      // VERIFY
      Assertions.assertEquals(Map.of(cashHolding, new MonthlyReturns().setCurrency(cad.name()).setHoldingType(
          HoldingType.CASH).setReturns(generatedReturns)), actual);
    }
  }

  @Test
  void checkCurrency_checkResult() {
    // SETUP
    final var sut = mock(MonthlyReturnsCacheStorage.class);
    final var currency = Currency.CAD;

    doCallRealMethod().when(sut).checkCurrency(currency, Currency.USD);
    // ACT
    var actual = sut.checkCurrency(currency, Currency.USD);

    // VERIFY
    Assertions.assertEquals(currency.name(), actual);
  }

  @Test
  void checkCurrency_checkResult2() {
    // SETUP
    final var sut = mock(MonthlyReturnsCacheStorage.class);
    final Currency currency = null;

    doCallRealMethod().when(sut).checkCurrency(currency, Currency.CAD);
    // ACT
    var actual = sut.checkCurrency(currency, Currency.CAD);

    // VERIFY
    Assertions.assertEquals(Currency.CAD.name(), actual);
  }

}