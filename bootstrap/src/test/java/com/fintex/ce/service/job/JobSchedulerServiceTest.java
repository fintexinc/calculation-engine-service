package com.fintex.ce.service.job;

import com.fintex.ce.port.output.cache.CacheCleanupPort;
import com.fintex.ce.port.output.cache.CacheWarmUpPort;
import com.fintex.ce.util.SchedulerUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

class JobSchedulerServiceTest {

  private static final LocalDate DEFAULT_DAY = LocalDate.of(2020, 1, 1);

  @BeforeAll
  static void setup() {
    TimeZone.setDefault(TimeZone.getTimeZone("EST"));
  }

  @Test
  void cacheWarmUp_verifyCacheWarmUpServiceRun() {
    // SETUP
    final var cacheWarmUpPort = mock(CacheWarmUpPort.class);
    final var cacheCleanupPort = mock(CacheCleanupPort.class);
    final var sut = mock(
        JobSchedulerService.class,
        withSettings().useConstructor(cacheWarmUpPort, cacheCleanupPort));

    doCallRealMethod().when(sut).warmUpRedisCache();

    // ACT
    sut.warmUpRedisCache();

    // VERIFY
    verify(cacheWarmUpPort).run();
  }

  @Test
  void warmUpRedisCache_verifyRunOncePerDay() {
    final LocalDateTime startOfTheDayDate = DEFAULT_DAY.atStartOfDay();
    final CronExpression caсheWarmUpScheduler = SchedulerUtil.getGenerator(JobSchedulerService.class,
        "warmUpRedisCache");

    // first execution should be in the same day
    final LocalDateTime firstExecutionDate = caсheWarmUpScheduler.next(startOfTheDayDate);
    assertEquals(startOfTheDayDate.toLocalDate(), firstExecutionDate.toLocalDate());

    // second execution should be in different days
    final LocalDateTime secondExecutionDate = caсheWarmUpScheduler.next(firstExecutionDate);
    assertNotEquals(startOfTheDayDate.toLocalDate(), secondExecutionDate.toLocalDate());
  }

  @Test
  void FDVTaskSourceCeSchedulerShouldRunAtSpecificTime_warmUpRedisCache() {
    final LocalDateTime startOfTheDayDate = DEFAULT_DAY.atStartOfDay();
    final LocalDateTime expectedExecutionDate = LocalDateTime.of(DEFAULT_DAY, LocalTime.of(3, 0));

    final CronExpression cacheWarmUpScheduler = SchedulerUtil.getGenerator(JobSchedulerService.class,
        "warmUpRedisCache");

    final LocalDateTime nextExecutionDate = cacheWarmUpScheduler.next(startOfTheDayDate);
    assertEquals(nextExecutionDate, expectedExecutionDate);
  }

  @Test
  void clearCaffeineCache_verifyRunOncePerDay() {
    final LocalDateTime startOfTheDayDate = DEFAULT_DAY.atStartOfDay();
    final CronExpression caсheWarmUpScheduler = SchedulerUtil.getGenerator(JobSchedulerService.class,
        "warmUpRedisCache");

    // first execution should be in the same day
    final LocalDateTime firstExecutionDate = caсheWarmUpScheduler.next(startOfTheDayDate);
    assertEquals(startOfTheDayDate.toLocalDate(), firstExecutionDate.toLocalDate());

    // second execution should be in different days
    final LocalDateTime secondExecutionDate = caсheWarmUpScheduler.next(firstExecutionDate);
    assertNotEquals(startOfTheDayDate.toLocalDate(), secondExecutionDate.toLocalDate());
  }

  @Test
  void FDVTaskSourceCeSchedulerShouldRunAtSpecificTime_clearCaffeineCache() {
    final LocalDateTime startOfTheDayDate = DEFAULT_DAY.atStartOfDay();
    final LocalDateTime expectedExecutionDate = LocalDateTime.of(DEFAULT_DAY, LocalTime.of(3, 0));

    final CronExpression cacheWarmUpScheduler = SchedulerUtil.getGenerator(JobSchedulerService.class,
        "warmUpRedisCache");

    final LocalDateTime nextExecutionDate = cacheWarmUpScheduler.next(startOfTheDayDate);
    assertEquals(nextExecutionDate, expectedExecutionDate);
  }

}
