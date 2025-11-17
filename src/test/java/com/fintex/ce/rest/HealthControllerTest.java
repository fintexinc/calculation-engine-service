package com.fintex.ce.rest;

import com.fintex.ce.repository.jdbc.FASUsageStatisticsRepo;
import com.fintex.ce.service.impl.cache.statistic.CacheWarmUpServiceImpl;
import com.fintex.ce.service.impl.cache.statistic.CacheWarmUpServiceImpl.SchedulerRunInfoDto;
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
        //SETUP
        final var fdsStatistics = mock(FASUsageStatisticsRepo.class);
        final var cacheWarmUpService = mock(CacheWarmUpServiceImpl.class);
        final var schedulerRunInfoDto = mock(SchedulerRunInfoDto.class);
        final var zonedDayTime = ZonedDateTime.of(LocalDateTime.MIN, ZoneId.of("UTC"));

        final var sut = mock(HealthController.class,
                withSettings().useConstructor(fdsStatistics, cacheWarmUpService));

        when(schedulerRunInfoDto.getLastTimeRun()).thenReturn(zonedDayTime);
        when(schedulerRunInfoDto.isRunInLast24Hours()).thenReturn(true);
        when(cacheWarmUpService.cacheWarmUpSchedulerRunCheck()).thenReturn(schedulerRunInfoDto);
        doCallRealMethod().when(sut).cacheWarmupHealthCheck();

        //ACT
        final var response = sut.cacheWarmupHealthCheck();

        //VERIFY
        verify(fdsStatistics).isDbHealthy();
        verify(cacheWarmUpService).cacheWarmUpSchedulerRunCheck();
        verify(schedulerRunInfoDto).isRunInLast24Hours();

        final String expectedBody = HEALTHY_RESPONSE + ", last time cache warm-up scheduler run: "+zonedDayTime;
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedBody, response.getBody());
    }

    @Test
    void getF5HealthCheck_checkResult_whenFdsRepositoryUnhealthy() {
        //SETUP
        final var fdsStatistics = mock(FASUsageStatisticsRepo.class);
        final var cacheWarmUpService = mock(CacheWarmUpServiceImpl.class);

        final var sut = mock(HealthController.class,
                withSettings().useConstructor(fdsStatistics, cacheWarmUpService));

        when(fdsStatistics.isDbHealthy()).thenThrow(new RuntimeException());
        doCallRealMethod().when(sut).cacheWarmupHealthCheck();

        //ACT
        final var response = sut.cacheWarmupHealthCheck();

        //VERIFY
        verify(fdsStatistics).isDbHealthy();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ERROR_RESPONSE, response.getBody());
    }

    @Test
    void getF5HealthCheck_checkResult_whenCacheWarmUpServiceUnhealthy() {
        //SETUP
        final var fdsStatistics = mock(FASUsageStatisticsRepo.class);
        final var cacheWarmUpService = mock(CacheWarmUpServiceImpl.class);
        final var schedulerRunInfoDto = mock(SchedulerRunInfoDto.class);

        final var sut = mock(HealthController.class,
                withSettings().useConstructor(fdsStatistics, cacheWarmUpService));

        when(schedulerRunInfoDto.isRunInLast24Hours()).thenReturn(false);
        when(cacheWarmUpService.cacheWarmUpSchedulerRunCheck()).thenReturn(schedulerRunInfoDto);
        doCallRealMethod().when(sut).cacheWarmupHealthCheck();

        //ACT
        final var response = sut.cacheWarmupHealthCheck();

        //VERIFY
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        final String expectedBody = ERROR_RESPONSE+", cache warm-up scheduler didn't run in past 24 hours";
        assertEquals(expectedBody, response.getBody());
    }

}