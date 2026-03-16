package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.YieldCacheStorage;
import com.fintex.ce.adapter.cache.entity.RYield;
import com.fintex.ce.adapter.cache.repository.YieldRepository;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.FIXED_INCOME_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.SEPARATELY_MANAGED_ACCOUNT_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class YieldCacheStorageTest {

  @Test
  void load_verifyFilters() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      final var securityDataPort = mock(SecurityDataPort.class);
      final CacheEntityMapper<Yield, RYield> mapper = mock(CacheEntityMapper.class);
      final var yieldRepository = mock(YieldRepository.class);

      final YieldCacheStorage m = mock(YieldCacheStorage.class, withSettings()
          .useConstructor(securityDataPort, mapper, yieldRepository));

      final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
      final List<Holding> holdings = List.of(holding);

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      List<Holding> holdingsFromMap = new ArrayList<>(holdings);

      m.load(holdingsFromMap, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_POOLED_FUND_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_MUTUAL_FUND_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_HEDGE_FUND_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(FIXED_INCOME_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(
          SEPARATELY_MANAGED_ACCOUNT_PREDICATE)));
      verify(m, times((9))).verify(anyMap(), anyList());
    }
  }

  @Test
  void load_verifyLoadBenchOfFundCanada() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final YieldCacheStorage m = mock(YieldCacheStorage.class);
      final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
      final List<Holding> holdings = List.of(holding);
      final var fundSeriesHolding = new FundSeriesHolding().setFundServCode("TEST");
      final List<FundSeriesHolding> filtered = List.of(fundSeriesHolding);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(m).loadBenchOfFundCanada(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfEtfUs() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final YieldCacheStorage m = mock(YieldCacheStorage.class);
      final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
      final List<Holding> holdings = List.of(holding);
      var etfHolding = new EtfHolding().setTicker("TEST").setExchangeCode("TST");
      final List<EtfHolding> filtered = List.of(etfHolding);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE))).thenReturn(filtered);

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(m).loadForBenchOfEtfUs(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfEtfCanada() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final YieldCacheStorage m = mock(YieldCacheStorage.class);
      final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
      final List<Holding> holdings = List.of(holding);
      var etfHolding = new EtfHolding().setTicker("TEST").setExchangeCode("TST");
      final List<EtfHolding> filtered = List.of(etfHolding);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(m).loadForBenchOfEtfCanada(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadGics() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final YieldCacheStorage m = mock(YieldCacheStorage.class);
      final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
      final List<Holding> holdings = List.of(holding);
      final var gicHolding = new GicHolding();
      gicHolding.setType(HoldingType.GIC);
      gicHolding.setName("name");
      gicHolding.setClientIntRate(BigDecimal.ONE);
      final List<GicHolding> filtered = List.of(gicHolding);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(GIC_PREDICATE))).thenReturn(filtered);

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      final Map<Holding, Yield> result = m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      Assertions.assertNotNull(result);
      Assertions.assertEquals(1, result.size());
      Assertions.assertTrue(result.containsKey(gicHolding));
    }
  }

  @Test
  void load_verifyLoadBenchOfFundCanada_yieldMapper() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final YieldCacheStorage m = mock(YieldCacheStorage.class);
      final Yield rYield = mock(Yield.class);
      final var fundSeriesHolding = new FundSeriesHolding().setFundServCode("TEST");
      final List<Holding> holdings = List.of();
      final List<FundSeriesHolding> filtered = List.of(fundSeriesHolding);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE))).thenReturn(
          filtered);

      when(rYield.getDividendYield()).thenReturn(null);
      when(m.loadBenchOfFundCanada(anyList(), anyList())).thenReturn(Map.of(fundSeriesHolding, rYield));
      doCallRealMethod().when(m).load(any(), any(), any(), any());
      doCallRealMethod().when(m).verify(any(), any());
      // ACT
      final List<Warning> warnings = new ArrayList<>();

      final Map<Holding, Yield> result = m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      Assertions.assertNotNull(result);
      Assertions.assertEquals(1, warnings.size());
    }
  }

  @Test
  void load_verifyLoadGics2() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final YieldCacheStorage m = mock(YieldCacheStorage.class);
      final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
      final List<Holding> holdings = List.of(holding);
      final var gicHolding = new GicHolding();
      gicHolding.setType(HoldingType.GIC);
      gicHolding.setName("name");
      gicHolding.setClientIntRate(BigDecimal.ONE);
      final List<GicHolding> filtered = List.of(gicHolding);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(GIC_PREDICATE))).thenReturn(filtered);

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      final Map<Holding, Yield> result = m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      Assertions.assertNotNull(result);
      Assertions.assertEquals(1, result.size());
      Assertions.assertTrue(result.containsKey(gicHolding));
    }
  }

  @Test
  void load_verifyLoadBenchOfFundCanada_yieldMapper2() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final YieldCacheStorage m = mock(YieldCacheStorage.class);
      final Yield rYield = mock(Yield.class);
      final var fundSeriesHolding = new FundSeriesHolding().setFundServCode("TEST");
      final List<Holding> holdings = List.of();
      final List<FundSeriesHolding> filtered = List.of(fundSeriesHolding);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE))).thenReturn(
          filtered);

      when(rYield.getDividendYield()).thenReturn(BigDecimal.ONE);
      when(m.loadBenchOfFundCanada(anyList(), anyList())).thenReturn(Map.of(fundSeriesHolding, rYield));
      doCallRealMethod().when(m).load(any(), any(), any(), any());
      doCallRealMethod().when(m).verify(any(), any());
      // ACT
      final List<Warning> warnings = new ArrayList<>();

      final Map<Holding, Yield> result = m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      Assertions.assertNotNull(result);
      Assertions.assertEquals(0, warnings.size());
    }
  }

}
