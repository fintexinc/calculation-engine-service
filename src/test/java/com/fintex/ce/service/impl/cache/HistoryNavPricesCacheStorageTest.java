package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RHistoricalNavPrices;
import com.fintex.ce.repository.graphql.query.HistoricalNavPricesSMRepository;
import com.fintex.ce.repository.redis.HistoricalNavPricesRedisRepository;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

class HistoryNavPricesCacheStorageTest {


    @Test
    void loadFxRates_checkResultWhenRedisCacheFxRates() {
        //SETUP
        final var historicalNavPricesFDSRepository = mock(HistoricalNavPricesSMRepository.class);
        final var historicalNavPricesRedisRepository = mock(HistoricalNavPricesRedisRepository.class);
        final var cacheStatisticService = mock(CacheStatisticService.class);
        final var sut = mock(HistoryNavPricesCacheStorage.class,
                withSettings().useConstructor(historicalNavPricesFDSRepository, historicalNavPricesRedisRepository,cacheStatisticService));

        final Holding holding = mock(Holding.class);
        final ParamHolderDTO paramHolderDTO = mock(ParamHolderDTO.class);


        doCallRealMethod().when(sut).load(any(), any(), any(), any());
        //ACT
        final Map<Holding, RHistoricalNavPrices> result = sut.load(
                List.of(holding),
                List.of(DataProvider.ENVESTNET),
                List.of(),
                paramHolderDTO
        );

        //VERIFY
        Objects.nonNull(result);
        Assertions.assertNotNull(result);
    }

    @Test
    void load_verifyLoadBenchOfFundCanada() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final HistoryNavPricesCacheStorage m = mock(HistoryNavPricesCacheStorage.class);
            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<FundSeriesHolding> filtered = List.of(mock(FundSeriesHolding.class));

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(m).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));

            m.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(m).loadBenchOfFundCanada(filtered, List.of());
        }
    }

}
