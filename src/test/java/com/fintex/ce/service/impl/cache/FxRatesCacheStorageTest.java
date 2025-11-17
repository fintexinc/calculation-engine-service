package com.fintex.ce.service.impl.cache;

import com.fintex.ce.model.redis.RFxRates;
import com.fintex.ce.repository.redis.FxRatesRepository;
import com.fintex.ce.util.ComparisonUtils;
import com.fintex.smclient.dto.FxRatesDTO;
import com.fintex.smclient.service.CommonEndpointsComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

class FxRatesCacheStorageTest {

    @Test
    void loadFxRates_checkResultWhenRedisCacheFxRates() {
        //SETUP
        final var commonEndpointsComponent = mock(CommonEndpointsComponent.class);
        final var fxRatesRepository = mock(FxRatesRepository.class);
        final var sut = mock(FxRatesCacheStorage.class,
                withSettings().useConstructor(commonEndpointsComponent, fxRatesRepository));

        final RFxRates rFxRates = new RFxRates();
        final var fxRatesFromRedisCache = mock(Map.class);
        rFxRates.setFxRates(fxRatesFromRedisCache);
        final var fxRatesFromCache = List.of(rFxRates);

        when(fxRatesRepository.findAllByPrefixEnv()).thenReturn(fxRatesFromCache);

        doCallRealMethod().when(sut).loadFxRates();
        //ACT
        final Map<LocalDate, FxRatesDTO> actual = sut.loadFxRates();

        //VERIFY
        Assertions.assertNotNull(actual);
        ComparisonUtils.compareMaps(fxRatesFromRedisCache, actual);
    }

    @Test
    void loadFxRates_checkResultWhenFdsFxRates() {
        //SETUP
        final var commonEndpointsComponent = mock(CommonEndpointsComponent.class);
        final var fxRatesRepository = mock(FxRatesRepository.class);
        final var sut = mock(FxRatesCacheStorage.class,
                withSettings().useConstructor(commonEndpointsComponent, fxRatesRepository));

        final var fxRatesFromCache = mock(List.class);
        final var fxRatesFromFds = mock(Map.class);

        when(fxRatesFromCache.isEmpty()).thenReturn(true);
        when(fxRatesRepository.findAllByPrefixEnv()).thenReturn(fxRatesFromCache);
        when(commonEndpointsComponent.loadFxRates()).thenReturn(fxRatesFromFds);

        doCallRealMethod().when(sut).loadFxRates();
        //ACT
        final Map<LocalDate, FxRatesDTO> actual = sut.loadFxRates();

        //VERIFY
        Assertions.assertNotNull(actual);
        ComparisonUtils.compareMaps(fxRatesFromFds, actual);
    }

    @Test
    void loadFxRates_verifyRedisFxRepositorySaveFdsFxRates() {
        //SETUP
        final var commonEndpointsComponent = mock(CommonEndpointsComponent.class);
        final var fxRatesRepository = mock(FxRatesRepository.class);
        final var sut = mock(FxRatesCacheStorage.class,
                withSettings().useConstructor(commonEndpointsComponent, fxRatesRepository));

        final var fxRatesFromCache = mock(List.class);
        final var fxRatesFromFds = mock(Map.class);

        when(fxRatesFromCache.isEmpty()).thenReturn(true);
        when(fxRatesRepository.findAllByPrefixEnv()).thenReturn(fxRatesFromCache);
        when(commonEndpointsComponent.loadFxRates()).thenReturn(fxRatesFromFds);

        doCallRealMethod().when(sut).loadFxRates();
        //ACT
        sut.loadFxRates();

        //VERIFY
        verify(fxRatesRepository).save(new RFxRates(fxRatesFromFds));
    }

    @Test
    void loadFxRates_verifyFdsLoadFxRates() {
        //SETUP
        final var commonEndpointsComponent = mock(CommonEndpointsComponent.class);
        final var fxRatesRepository = mock(FxRatesRepository.class);
        final var sut = mock(FxRatesCacheStorage.class,
                withSettings().useConstructor(commonEndpointsComponent, fxRatesRepository));

        final var fxRatesFromCache = mock(List.class);

        when(fxRatesFromCache.isEmpty()).thenReturn(true);
        when(fxRatesRepository.findAllByPrefixEnv()).thenReturn(fxRatesFromCache);

        doCallRealMethod().when(sut).loadFxRates();
        //ACT
        sut.loadFxRates();

        //VERIFY
        verify(commonEndpointsComponent).loadFxRates();
    }

    @Test
    void loadFxRates_verifyFxRatesRepositoryFindAll() {
        //SETUP
        final var commonEndpointsComponent = mock(CommonEndpointsComponent.class);
        final var fxRatesRepository = mock(FxRatesRepository.class);
        final var sut = mock(FxRatesCacheStorage.class,
                withSettings().useConstructor(commonEndpointsComponent, fxRatesRepository));

        doCallRealMethod().when(sut).loadFxRates();
        //ACT
        sut.loadFxRates();

        //VERIFY
        verify(fxRatesRepository).findAllByPrefixEnv();
    }

}