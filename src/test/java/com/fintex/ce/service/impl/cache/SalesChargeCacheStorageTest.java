package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RSalesCharge;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class SalesChargeCacheStorageTest {

    @Test
    void load_verifyLoadBenchOfFundCanada() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var sut = mock(SalesChargeCacheStorage.class);

            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<DataProvider> providers = mock(List.class);
            final ParamHolderDTO paramHolderDTO = mock(ParamHolderDTO.class);
            final List<Warning> warnings = mock(List.class);
            final List<FundSeriesHolding> filtered = mock(List.class);

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(holdings, CANADA_MUTUAL_PREDICATE)).thenReturn(filtered);

            doCallRealMethod().when(sut).load(anyList(), anyList(), anyList(), any());
            //ACT
            sut.load(holdings, providers, warnings, paramHolderDTO);

            //VERIFY
            verify(sut).loadBenchOfFundCanada(filtered, List.of());
        }
    }

    @Test
    void load_checkResult() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var sut = mock(SalesChargeCacheStorage.class);

            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<DataProvider> providers = mock(List.class);
            final ParamHolderDTO paramHolderDTO = mock(ParamHolderDTO.class);
            final List<Warning> warnings = mock(List.class);
            final List<FundSeriesHolding> filtered = mock(List.class);
            final Map benchOfFundCanada = mock(Map.class);

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(holdings, CANADA_MUTUAL_PREDICATE)).thenReturn(filtered);
            when(sut.loadBenchOfFundCanada(anyList(), any())).thenReturn(benchOfFundCanada);

            doCallRealMethod().when(sut).load(anyList(), anyList(), anyList(), any());
            //ACT
            final Map<Holding, RSalesCharge> actual = sut.load(holdings, providers, warnings, paramHolderDTO);

            //VERIFY
            assertEquals(new HashMap<>(benchOfFundCanada), actual);
        }
    }

}
