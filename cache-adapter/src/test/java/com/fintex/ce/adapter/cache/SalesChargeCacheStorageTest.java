package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.SalesChargeCacheStorage;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.SalesCharge;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class SalesChargeCacheStorageTest {

  @Test
  void load_verifyLoadBenchOfFundCanada() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(SalesChargeCacheStorage.class);

      final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
      final List<Holding> holdings = List.of(holding);
      final List<DataProvider> providers = mock(List.class);
      final ParamHolderDTO paramHolderDTO = mock(ParamHolderDTO.class);
      final List<Warning> warnings = mock(List.class);
      final List<FundSeriesHolding> filtered = mock(List.class);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(holdings, CANADA_MUTUAL_PREDICATE)).thenReturn(filtered);

      doCallRealMethod().when(sut).load(anyList(), anyList(), anyList(), any());
      // ACT
      sut.load(holdings, providers, warnings, paramHolderDTO);

      // VERIFY
      verify(sut).loadBenchOfFundCanada(filtered, List.of());
    }
  }

  @Test
  void load_checkResult() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(SalesChargeCacheStorage.class);

      final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
      final List<Holding> holdings = List.of(holding);
      final List<DataProvider> providers = mock(List.class);
      final ParamHolderDTO paramHolderDTO = mock(ParamHolderDTO.class);
      final List<Warning> warnings = mock(List.class);
      final List<FundSeriesHolding> filtered = mock(List.class);
      final Map benchOfFundCanada = mock(Map.class);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(holdings, CANADA_MUTUAL_PREDICATE)).thenReturn(filtered);
      when(sut.loadBenchOfFundCanada(anyList(), any())).thenReturn(benchOfFundCanada);

      doCallRealMethod().when(sut).load(anyList(), anyList(), anyList(), any());
      // ACT
      final Map<Holding, SalesCharge> actual = sut.load(holdings, providers, warnings, paramHolderDTO);

      // VERIFY
      assertEquals(new HashMap<>(benchOfFundCanada), actual);
    }
  }

}
