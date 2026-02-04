package com.fintex.ce.application.service;

import com.fintex.ce.domain.exception.DataErrorException;
import com.fintex.ce.adapter.cache.entity.core.RedisId;
import com.fintex.ce.adapter.cache.repository.FxRatesRepository;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import com.fintex.ce.service.ExceptionHandlingService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.fintex.ce.domain.enumeration.ExceptionCode.FX_RATE_EXCEPTION_CODES;

@Log4j2
@Service
public class ExceptionHandlingServiceImpl implements ExceptionHandlingService {

  private final List<CoreRedisCacheRepository> coreRedisCacheRepositories;
  private final FxRatesRepository fxRatesRepository;

  @Autowired
  public ExceptionHandlingServiceImpl(final List<CoreRedisCacheRepository> coreRedisCacheRepositories,
      final FxRatesRepository fxRatesRepository) {
    this.coreRedisCacheRepositories = coreRedisCacheRepositories;
    this.fxRatesRepository = fxRatesRepository;
  }

  @Override
  public void removeFxRatesFromRedisCache() {
    fxRatesRepository.deleteAll();
    log.info("Remove fx rates from redis cache");
  }

  @SuppressWarnings("unchecked")
  public void removeDataFromRepositoriesByHoldingId(final String holdingId) {
    coreRedisCacheRepositories.forEach(repository -> {
      final List<RedisId> redisIds = repository.findAllByHoldingId(holdingId);
      redisIds.forEach(id -> repository.deleteById(id.getId()));
    });
  }

  @Override
  public void ifFxRatesErrorRemoveFxRatesFromRedisCache(final DataErrorException e) {
    FX_RATE_EXCEPTION_CODES.stream()
        .filter(exceptionCode -> exceptionCode.equals(e.getCode()))
        .forEach(exceptionCode -> CompletableFuture.runAsync(this::removeFxRatesFromRedisCache));
  }
}