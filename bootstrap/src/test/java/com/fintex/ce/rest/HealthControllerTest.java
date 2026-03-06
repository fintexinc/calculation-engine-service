package com.fintex.ce.rest;

import com.fintex.ce.adapter.jdbc.repository.FASUsageStatisticsRepo;
import com.fintex.ce.adapter.rest.controller.HealthController;
import com.fintex.ce.port.output.cache.CacheWarmUpPort;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class HealthControllerTest {

  private static final String HEALTHY_RESPONSE = "healthy";
  private static final String ERROR_RESPONSE = "error";

  @Test
  void getF5HealthCheck_checkResult_whenAllAreHealthy() {
    // SETUP
    final var fdsStatistics = mock(FASUsageStatisticsRepo.class);
    final var cacheWarmUpPort = mock(CacheWarmUpPort.class);
    final var zonedDayTime = ZonedDateTime.of(LocalDateTime.MIN, ZoneId.of("UTC"));

    final var sut = mock(HealthController.class,
        withSettings().useConstructor(fdsStatistics, cacheWarmUpPort));

    final var schedulerRunInfo = new CacheWarmUpPort.SchedulerRunInfo(true, zonedDayTime);
    when(cacheWarmUpPort.cacheWarmUpSchedulerRunCheck()).thenReturn(schedulerRunInfo);
    doCallRealMethod().when(sut).cacheWarmupHealthCheck();

    // ACT
    final var response = sut.cacheWarmupHealthCheck();

    // VERIFY
    verify(fdsStatistics).isDbHealthy();
    verify(cacheWarmUpPort).cacheWarmUpSchedulerRunCheck();

    final String expectedBody = HEALTHY_RESPONSE + ", last time cache warm-up scheduler run: " + zonedDayTime;
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(expectedBody, response.getBody());
  }

  @Test
  void getF5HealthCheck_checkResult_whenFdsRepositoryUnhealthy() {
    // SETUP
    final var fdsStatistics = mock(FASUsageStatisticsRepo.class);
    final var cacheWarmUpPort = mock(CacheWarmUpPort.class);

    final var sut = mock(HealthController.class,
        withSettings().useConstructor(fdsStatistics, cacheWarmUpPort));

    when(fdsStatistics.isDbHealthy()).thenThrow(new RuntimeException());
    doCallRealMethod().when(sut).cacheWarmupHealthCheck();

    // ACT
    final var response = sut.cacheWarmupHealthCheck();

    // VERIFY
    verify(fdsStatistics).isDbHealthy();

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals(ERROR_RESPONSE, response.getBody());
  }

  @Test
  void getF5HealthCheck_checkResult_whenCacheWarmUpServiceUnhealthy() {
    // SETUP
    final var fdsStatistics = mock(FASUsageStatisticsRepo.class);
    final var cacheWarmUpPort = mock(CacheWarmUpPort.class);

    final var sut = mock(HealthController.class,
        withSettings().useConstructor(fdsStatistics, cacheWarmUpPort));

    final var schedulerRunInfo = new CacheWarmUpPort.SchedulerRunInfo(false, null);
    when(cacheWarmUpPort.cacheWarmUpSchedulerRunCheck()).thenReturn(schedulerRunInfo);
    doCallRealMethod().when(sut).cacheWarmupHealthCheck();

    // ACT
    final var response = sut.cacheWarmupHealthCheck();

    // VERIFY
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    final String expectedBody = ERROR_RESPONSE + ", cache warm-up scheduler didn't run in past 24 hours";
    assertEquals(expectedBody, response.getBody());
  }

}