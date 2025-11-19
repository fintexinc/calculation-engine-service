package com.fintex.ce.service.impl.cache;

import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.repository.graphql.query.HistoricalDistributionsSMRepository;
import com.fintex.ce.repository.redis.HistoricalDistributionsRedisRepository;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

class HistoricalDistributionsCacheStorageTest {

    @Test
    void load_verifyFilters() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var queryRepository = mock(HistoricalDistributionsSMRepository.class);
            final var fixedIncomeBondSectorRepository = mock(HistoricalDistributionsRedisRepository.class);
            final var cacheStatisticService = mock(CacheStatisticService.class);

            final var sut = mock(HistoricalDistributionsCacheStorage.class, withSettings()
                    .useConstructor(queryRepository, fixedIncomeBondSectorRepository, cacheStatisticService));

            final List<Holding> holdings = List.of(mock(Holding.class));

            doCallRealMethod().when(sut).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));
            sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE)));
        }
    }

}
