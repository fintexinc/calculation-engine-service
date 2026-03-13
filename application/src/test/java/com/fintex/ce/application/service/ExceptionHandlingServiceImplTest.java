package com.fintex.ce.application.service;

import com.fintex.ce.port.output.cache.CacheCleanupPort;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

class ExceptionHandlingServiceImplTest {

  @Test
  void shouldRemoveFxRatesFromRedisCache_whenVerifyCacheCleanupPortRemoveFxRatesFromCache() {
    // SETUP
    final CacheCleanupPort cacheCleanupPort = mock(CacheCleanupPort.class);
    final var sut = mock(ExceptionHandlingServiceImpl.class,
        withSettings().useConstructor(cacheCleanupPort));

    doCallRealMethod().when(sut).removeFxRatesFromRedisCache();
    // ACT
    sut.removeFxRatesFromRedisCache();

    // VERIFY
    verify(cacheCleanupPort).removeFxRatesFromCache();
  }

  @Test
  void shouldRemoveDataFromRepositoriesByHoldingId_whenVerifyRemoveByHoldingId() {
    // SETUP
    final String id = "id";
    final CacheCleanupPort cacheCleanupPort = mock(CacheCleanupPort.class);
    final ExceptionHandlingServiceImpl sut = mock(ExceptionHandlingServiceImpl.class,
        withSettings().useConstructor(cacheCleanupPort));
    doCallRealMethod().when(sut).removeDataFromRepositoriesByHoldingId(any());

    // ACT
    sut.removeDataFromRepositoriesByHoldingId(id);

    // VERIFY
    verify(cacheCleanupPort).removeByHoldingId(id);
  }

}