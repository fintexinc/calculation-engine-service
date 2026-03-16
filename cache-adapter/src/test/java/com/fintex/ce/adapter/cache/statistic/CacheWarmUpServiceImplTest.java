package com.fintex.ce.adapter.cache.statistic;

import java.time.ZonedDateTime;
import java.util.List;

import com.fintex.ce.adapter.cache.entity.RCacheWarmUpDate;
import com.fintex.ce.adapter.cache.repository.CacheWarmUpSchedulerDateRedisRepository;
import com.fintex.ce.port.output.cache.CacheCleanupPort;
import com.fintex.ce.port.output.cache.CacheWarmUpPort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

class CacheWarmUpServiceImplTest {

  @Test
  void run_verifyClearCacheAndSaveDate() {
    // SETUP
    final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
    final var cacheCleanupPort = mock(CacheCleanupPort.class);
    final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
        .useConstructor(cacheWarmUpSchedulerDateRedisRepository, cacheCleanupPort));

    doCallRealMethod().when(sut).run();
    // ACT
    sut.run();

    // VERIFY
    verify(cacheCleanupPort).clearCache();
    verify(cacheWarmUpSchedulerDateRedisRepository).save(org.mockito.ArgumentMatchers.any(RCacheWarmUpDate.class));
  }

  @Test
  void cacheWarmUpSchedulerRunCheck_checkResultSchedulerRunLessThan10HoursAgo() {
    // SETUP
    final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
    final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
        .useConstructor(cacheWarmUpSchedulerDateRedisRepository, null));

    final ZonedDateTime nowMinus10Hours = ZonedDateTime.now().minusHours(10);
    final RCacheWarmUpDate rCacheWarmUpDate = new RCacheWarmUpDate().setZonedDateTime(nowMinus10Hours);
    final var expected = new CacheWarmUpPort.SchedulerRunInfo(true, nowMinus10Hours);

    doReturn(List.of(rCacheWarmUpDate)).when(cacheWarmUpSchedulerDateRedisRepository).findAllByPrefixEnv();

    doCallRealMethod().when(sut).cacheWarmUpSchedulerRunCheck();
    // ACT
    final CacheWarmUpPort.SchedulerRunInfo actual = sut.cacheWarmUpSchedulerRunCheck();

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void cacheWarmUpSchedulerRunCheck_checkResultSchedulerDidntRun() {
    // SETUP
    final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
    final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
        .useConstructor(cacheWarmUpSchedulerDateRedisRepository, null));
    final var expected = new CacheWarmUpPort.SchedulerRunInfo(false, null);

    doReturn(List.of()).when(cacheWarmUpSchedulerDateRedisRepository).findAll();

    doCallRealMethod().when(sut).cacheWarmUpSchedulerRunCheck();
    // ACT
    final CacheWarmUpPort.SchedulerRunInfo actual = sut.cacheWarmUpSchedulerRunCheck();

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void cacheWarmUpSchedulerRunCheck_checkResultSchedulerDidntRunIn24Hours() {
    // SETUP
    final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
    final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
        .useConstructor(cacheWarmUpSchedulerDateRedisRepository, null));

    final ZonedDateTime zonedDateTime = ZonedDateTime.now().minusHours(25);
    final RCacheWarmUpDate rCacheWarmUpDate = new RCacheWarmUpDate().setZonedDateTime(zonedDateTime);
    final var expected = new CacheWarmUpPort.SchedulerRunInfo(false, zonedDateTime);

    doReturn(List.of(rCacheWarmUpDate)).when(cacheWarmUpSchedulerDateRedisRepository).findAllByPrefixEnv();

    doCallRealMethod().when(sut).cacheWarmUpSchedulerRunCheck();
    // ACT
    final CacheWarmUpPort.SchedulerRunInfo actual = sut.cacheWarmUpSchedulerRunCheck();

    // VERIFY
    assertEquals(expected, actual);
  }

}
