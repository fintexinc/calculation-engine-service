package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.IncomeForecastCacheStorage;
import com.fintex.ce.adapter.cache.entity.RIncomeForecast;
import com.fintex.ce.adapter.cache.repository.IncomeForecastRepository;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.IncomeForecast;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.smclient.graphql.PaymentFrequencyType;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_FI_DY_001;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_FI_ID_001;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_FI_MD_001;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_FI_PF_001;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_FI_SC_001;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.FIXED_INCOME_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

class IncomeForecastCacheStorageTest {

  @Test
  void load_verifyFilters() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var securityDataPort = mock(SecurityDataPort.class);
      final CacheEntityMapper<IncomeForecast, RIncomeForecast> mapper = mock(CacheEntityMapper.class);
      final var incomeForecastRepository = mock(IncomeForecastRepository.class);

      final IncomeForecastCacheStorage m = mock(IncomeForecastCacheStorage.class, withSettings()
          .useConstructor(securityDataPort, mapper, incomeForecastRepository));

      final List<Holding> holdings = List.of(new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE));

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      Map<Holding, IncomeForecast> holdingExposureMap = holdings.stream()
          .collect(Collectors.toMap(holding -> holding, holding -> mock(IncomeForecast.class)));

      List<Holding> holdingsFromMap = new ArrayList<>(holdingExposureMap.keySet());

      m.load(holdingsFromMap, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_POOLED_FUND_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_MUTUAL_FUND_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_HEDGE_FUND_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(FIXED_INCOME_PREDICATE)));
      verify(m, times((8))).verify(anyMap(), anyList());
    }
  }

  @Test
  void load_verifyLoadBenchOfFundCanada() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final IncomeForecastCacheStorage m = mock(IncomeForecastCacheStorage.class);
      final List<Holding> holdings = List.of(new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE));
      var fsh = new FundSeriesHolding().setFundServCode("TEST");
      final List<FundSeriesHolding> filtered = List.of(fsh);

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
      final IncomeForecastCacheStorage m = mock(IncomeForecastCacheStorage.class);
      final List<Holding> holdings = List.of(new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE));
      var etf = new EtfHolding().setTicker("TEST").setExchangeCode("TST");
      final List<EtfHolding> filtered = List.of(etf);

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
      final IncomeForecastCacheStorage m = mock(IncomeForecastCacheStorage.class);
      final List<Holding> holdings = List.of(new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE));
      var etf = new EtfHolding().setTicker("TEST").setExchangeCode("TST");
      final List<EtfHolding> filtered = List.of(etf);

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
  void incomeForecastMapper_checkResult() {
    // SETUP
    final IncomeForecastCacheStorage m = mock(IncomeForecastCacheStorage.class);
    final Holding h = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
    final Map.Entry<Holding, IncomeForecast> entry = mock(Map.Entry.class);

    when(entry.getKey()).thenReturn(h);

    final IncomeForecast rIncomeForecast = mock(IncomeForecast.class);
    when(entry.getValue()).thenReturn(rIncomeForecast);

    doCallRealMethod().when(m).incomeForecastMapper(any(), any());
    // ACT
    final List<Warning> warnings = new ArrayList<>();
    m.incomeForecastMapper(entry, warnings);

    // VERIFY
    assertEquals(1, warnings.size());
    assertEquals(WRN_FI_DY_001.getMessage(), warnings.get(0).getMessage());
    assertEquals(WRN_FI_DY_001.name(), warnings.get(0).getCode());
  }

  @Test
  void incomeForecastMapper_checkResult_2() {
    // SETUP
    final IncomeForecastCacheStorage m = mock(IncomeForecastCacheStorage.class);
    final Holding holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
    final Map.Entry<Holding, IncomeForecast> entry = mock(Map.Entry.class);
    final BigDecimal dividendYield = BigDecimal.TEN;

    when(entry.getKey()).thenReturn(holding);

    final IncomeForecast rIncomeForecast = mock(IncomeForecast.class);
    when(entry.getValue()).thenReturn(rIncomeForecast);
    when(rIncomeForecast.getDividendYield()).thenReturn(dividendYield);
    when(rIncomeForecast.getSchedule()).thenReturn(null);

    doCallRealMethod().when(m).incomeForecastMapper(any(), any());
    // ACT
    final List<Warning> warnings = new ArrayList<>();
    m.incomeForecastMapper(entry, warnings);

    // VERIFY
    assertEquals(1, warnings.size());
    assertEquals(WRN_FI_SC_001.getMessage(), warnings.get(0).getMessage());
    assertEquals(WRN_FI_SC_001.name(), warnings.get(0).getCode());
  }

  @Test
  void incomeForecastMapper_checkResult_3() {
    // SETUP
    final IncomeForecastCacheStorage m = mock(IncomeForecastCacheStorage.class);
    final Holding holding = new Holding().setType(HoldingType.FIXED_INCOME).setValue(BigDecimal.ONE);
    final Map.Entry<Holding, IncomeForecast> entry = mock(Map.Entry.class);
    final BigDecimal dividendYield = BigDecimal.TEN;

    when(entry.getKey()).thenReturn(holding);

    final IncomeForecast rIncomeForecast = mock(IncomeForecast.class);
    when(entry.getValue()).thenReturn(rIncomeForecast);
    when(rIncomeForecast.getDividendYield()).thenReturn(dividendYield);
    when(rIncomeForecast.getSchedule()).thenReturn(null);

    doCallRealMethod().when(m).incomeForecastMapper(any(), any());
    // ACT
    final List<Warning> warnings = new ArrayList<>();
    m.incomeForecastMapper(entry, warnings);

    // VERIFY
    assertEquals(2, warnings.size());
    assertEquals(WRN_FI_PF_001.getMessage(), warnings.get(0).getMessage());
    assertEquals(WRN_FI_PF_001.name(), warnings.get(0).getCode());
    assertEquals(WRN_FI_SC_001.getMessage(), warnings.get(1).getMessage());
    assertEquals(WRN_FI_SC_001.name(), warnings.get(1).getCode());
  }

  @Test
  void incomeForecastMapper_checkResult_4() {
    // SETUP
    final IncomeForecastCacheStorage m = mock(IncomeForecastCacheStorage.class);
    final Holding holding = new Holding().setType(HoldingType.FIXED_INCOME).setValue(BigDecimal.ONE);
    final Map.Entry<Holding, IncomeForecast> entry = mock(Map.Entry.class);
    final BigDecimal dividendYield = BigDecimal.TEN;
    final List<String> schedule = List.of("2024-01-01");

    when(entry.getKey()).thenReturn(holding);

    final IncomeForecast rIncomeForecast = mock(IncomeForecast.class);
    when(entry.getValue()).thenReturn(rIncomeForecast);
    when(rIncomeForecast.getDividendYield()).thenReturn(dividendYield);
    when(rIncomeForecast.getSchedule()).thenReturn(schedule);
    when(rIncomeForecast.getPaymentFrequencyType()).thenReturn(PaymentFrequencyType.AT_MATURITY.name());

    doCallRealMethod().when(m).incomeForecastMapper(any(), any());

    // ACT
    final List<Warning> warnings = new ArrayList<>();
    m.incomeForecastMapper(entry, warnings);

    // VERIFY
    assertEquals(2, warnings.size());
    assertEquals(WRN_FI_MD_001.getMessage(), warnings.get(0).getMessage());
    assertEquals(WRN_FI_MD_001.name(), warnings.get(0).getCode());
    assertEquals(WRN_FI_ID_001.getMessage(), warnings.get(1).getMessage());
    assertEquals(WRN_FI_ID_001.name(), warnings.get(1).getCode());
  }

}
