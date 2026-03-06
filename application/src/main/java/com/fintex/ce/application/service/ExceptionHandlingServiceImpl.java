package com.fintex.ce.application.service;

import com.fintex.ce.domain.exception.DataErrorException;
import com.fintex.ce.port.output.cache.CacheCleanupPort;
import com.fintex.ce.service.ExceptionHandlingService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static com.fintex.ce.domain.enumeration.ExceptionCode.FX_RATE_EXCEPTION_CODES;

@Log4j2
@Service
public class ExceptionHandlingServiceImpl implements ExceptionHandlingService {

  private final CacheCleanupPort cacheCleanupPort;

  @Autowired
  public ExceptionHandlingServiceImpl(final CacheCleanupPort cacheCleanupPort) {
    this.cacheCleanupPort = cacheCleanupPort;
  }

  @Override
  public void removeFxRatesFromRedisCache() {
    cacheCleanupPort.removeFxRatesFromCache();
  }

  public void removeDataFromRepositoriesByHoldingId(final String holdingId) {
    cacheCleanupPort.removeByHoldingId(holdingId);
  }

  @Override
  public void ifFxRatesErrorRemoveFxRatesFromRedisCache(final DataErrorException e) {
    FX_RATE_EXCEPTION_CODES.stream()
        .filter(exceptionCode -> exceptionCode.equals(e.getCode()))
        .forEach(exceptionCode -> CompletableFuture.runAsync(this::removeFxRatesFromRedisCache));
  }
}