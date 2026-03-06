package com.fintex.ce.adapter.cache.core;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.constant.CacheNameEntity;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.cache.entity.core.RedisId;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.cache.HoldingDataLoader;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticService;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static com.fintex.ce.util.CacheUtils.buildIdBasedOnProviders;
import static com.fintex.ce.util.CollectorUtils.toMap;

/**
 * Abstract base class for cache storage implementations. Simplified from MultipleCacheStorageAbstract with single type
 * parameter.
 *
 * @param <T>
 *          domain model type
 * @param <R>
 *          Redis entity type (extends RedisId)
 * @param <L>
 *          load return type (what {@link #load} returns)
 */
@Log4j2
public abstract class CacheStorageAbstract<T, R extends RedisId, L> implements HoldingDataLoader<L> {

  private final SecurityDataPort<T> securityDataPort;
  private final CacheEntityMapper<T, R> mapper;
  final CoreRedisCacheRepository<R> cacheRepo;

  private final CacheStatisticService cacheStatisticService;
  @Getter
  private final CacheNameEntity cacheNameEntity;

  protected CacheStorageAbstract(SecurityDataPort<T> securityDataPort,
      CacheEntityMapper<T, R> mapper,
      CoreRedisCacheRepository<R> cacheRepo,
      CacheStatisticService cacheStatisticService,
      CacheNameEntity cacheNameEntity) {
    this.securityDataPort = securityDataPort;
    this.mapper = mapper;
    this.cacheRepo = cacheRepo;
    this.cacheStatisticService = cacheStatisticService;
    this.cacheNameEntity = cacheNameEntity;
  }

  @Override
  public abstract L load(final List<? extends Holding> holdings, final List<DataProvider> providers,
      final List<Warning> warnings, final ParamHolderDTO paramHolderDTO);

  // ---- Per-type convenience methods (delegates to synchronizeAndLoad) ----

  public <H extends Holding> Map<H, T> loadBenchOfFundCanada(final List<H> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoad(holdings, providers, CacheCategory.CANADA_MUTUAL_FUNDS);
  }

  public <H extends Holding> Map<H, T> loadUsMutualFunds(final List<H> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoad(holdings, providers, CacheCategory.US_MUTUAL_FUNDS);
  }

  public <H extends Holding> Map<H, T> loadForBenchOfEtfCanada(final List<H> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoad(holdings, providers, CacheCategory.CANADA_ETF);
  }

  public <H extends Holding> Map<H, T> loadForBenchOfEtfUs(final List<H> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoad(holdings, providers, CacheCategory.US_ETF);
  }

  public <H extends Holding> Map<H, T> loadForBenchOfStock(final List<H> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoad(holdings, providers, CacheCategory.STOCKS);
  }

  public <H extends Holding> Map<H, T> loadForBenchOfBenchmarks(final List<H> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoad(holdings, providers, CacheCategory.BENCHMARK_INDEXES);
  }

  public <H extends Holding> Map<H, T> loadCanadaPooledFunds(final List<H> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoad(holdings, providers, CacheCategory.CANADA_POOLED_FUNDS);
  }

  public <H extends Holding> Map<H, T> loadCanadaHedgeFunds(final List<H> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoad(holdings, providers, CacheCategory.CANADA_HEDGE_FUNDS);
  }

  public <H extends Holding> Map<H, T> loadBenchOfFixedIncomes(final List<H> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoad(holdings, providers, CacheCategory.FIXED_INCOME);
  }

  public <H extends Holding> Map<H, T> loadBenchOfSeparatelyManagedAccounts(final List<H> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoad(holdings, providers, CacheCategory.SEPARATELY_MANAGED_ACCOUNT);
  }

  public <H extends Holding> Map<H, T> loadBenchOfPagGuidedPortfolios(final List<H> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoad(holdings, providers, CacheCategory.PAG_GUIDED_PORTFOLIO);
  }

  // ---- Core cache synchronization ----

  /**
   * Caches the data for holdings and performs the actual SM call if some holdings are not in the cache already. Handles
   * conversion between domain models and Redis entities.
   */
  @SuppressWarnings("unchecked")
  public <H extends Holding> Map<H, T> synchronizeAndLoad(
      final List<H> holdings,
      final List<DataProvider> providers,
      final CacheCategory cacheCategory) {
    if (holdings.isEmpty()) {
      return new HashMap<>();
    }

    // Load from cache (as Redis entities) and convert to domain models
    final Map<H, Optional<R>> cachedRedisEntities = loadCachesForHoldings(holdings, providers);
    final Map<H, R> filteredCachedEntities = filterCachedResponses(cachedRedisEntities);
    final Map<H, T> cachedDomainModels = convertToDomainMap(filteredCachedEntities);

    // Fetch uncached holdings from SM repository (returns domain models)
    final Map<H, T> fetchedDomainModels = fetchUncachedHoldings(
        holdings, providers, new ArrayList<>(cachedDomainModels.keySet()));

    // Merge cached and fetched results
    fetchedDomainModels.putAll(cachedDomainModels);

    // Analytics
    CompletableFuture.runAsync(() -> {
      Map<H, R> forAnalytics = convertToEntityMap(fetchedDomainModels);
      cacheStatisticService.analyse(forAnalytics, cacheNameEntity, cacheCategory);
    });

    return fetchedDomainModels;
  }

  @SuppressWarnings("unchecked")
  <H extends Holding> Map<H, T> fetchUncachedHoldings(
      final List<H> holdings,
      final List<DataProvider> providers,
      final List<H> cachedHoldings) {
    final List<H> uncachedHoldings = filterHoldingsBy(holdings, h -> !cachedHoldings.contains(h));
    if (uncachedHoldings.isEmpty()) {
      return new HashMap<>();
    }

    Map<Holding, T> smResult = securityDataPort.fetch(uncachedHoldings, providers);
    Map<H, T> uncachedDomainModels = new HashMap<>();
    for (Map.Entry<Holding, T> entry : smResult.entrySet()) {
      uncachedDomainModels.put((H) entry.getKey(), entry.getValue());
    }

    // Convert to Redis entities and filter out responses with errors
    final Map<H, R> uncachedRedisEntities = convertToEntityMap(uncachedDomainModels);
    final HashMap<H, R> entitiesWithoutErrors = filterResponsesWithoutAnyErrors(uncachedRedisEntities);

    // Save to cache
    saveToCache(entitiesWithoutErrors, providers);

    return uncachedDomainModels;
  }

  // ---- Helper methods ----

  <H, D> Map<H, D> convertToDomainMap(Map<H, R> entityMap) {
    Map<H, D> result = new HashMap<>();
    entityMap.forEach((holding, entity) -> {
      mapper.toDomain(entity).ifPresent(domain -> {
        @SuppressWarnings("unchecked")
        D typed = (D) domain;
        result.put(holding, typed);
      });
    });
    return result;
  }

  <H> Map<H, R> convertToEntityMap(Map<H, T> domainMap) {
    Map<H, R> result = new HashMap<>();
    domainMap.forEach((holding, domain) -> {
      mapper.toEntity(domain).ifPresent(entity -> result.put(holding, entity));
    });
    return result;
  }

  <H> HashMap<H, R> filterResponsesWithoutAnyErrors(Map<H, R> uncachedHoldingResponses) {
    return uncachedHoldingResponses.entrySet()
        .stream()
        .filter(e -> Objects.nonNull(e) && !e.getValue().hasErrors())
        .collect(toMap());
  }

  public <H extends Holding> Map<H, Optional<R>> loadCachesForHoldings(final List<H> holdings,
      final List<DataProvider> providers) {
    return holdings.stream().collect(toMap(e -> e, e -> queryCacheForHolding(e, providers)));
  }

  <H extends Holding> Optional<R> queryCacheForHolding(final H h,
      final List<DataProvider> providers) {
    if (!CollectionUtils.isEmpty(providers)) {
      return queryCacheForEnteredDataProviders(providers, h);
    }

    var start = System.currentTimeMillis();
    final List<R> all = cacheRepo.findAllByHoldingId(h.generateUserIdentifier());
    log.debug("getting data from cache for {}, took {}ms", h.generateUserIdentifier(), System.currentTimeMillis()
        - start);
    if (all.isEmpty()) {
      return Optional.empty();
    }
    return pickUpProviderBasedOnPriority(all);
  }

  Optional<R> pickUpProviderBasedOnPriority(final List<R> all) {
    for (DataProvider value : DataProvider.values()) {
      final Optional<R> first = all.stream().filter(r -> value.name().equalsIgnoreCase(r.getProvider())).findFirst();
      if (first.isPresent()) {
        return first;
      }
    }
    return Optional.of(all.get(0));
  }

  <H extends Holding> Optional<R> queryCacheForEnteredDataProviders(
      final List<DataProvider> providers,
      final H h) {
    final Optional<R> entity = cacheRepo.findOneByHoldingIdAndProviders(h.generateUserIdentifier(),
        buildIdBasedOnProviders(providers));
    if (entity.isEmpty() && providers.size() > 1) {
      return cacheRepo.findOneByHoldingIdAndProvider(h.generateUserIdentifier(), providers.get(0).name());
    }
    return entity;
  }

  <H> void saveToCache(final Map<H, R> uncachedHoldingResponses,
      final List<DataProvider> providers) {
    uncachedHoldingResponses.forEach((holding, response) -> {
      if (!CollectionUtils.isEmpty(providers)) {
        response.setProviders(buildIdBasedOnProviders(providers));
      }
      cacheRepo.save(response);
    });
  }

  <H> Map<H, R> filterCachedResponses(final Map<H, Optional<R>> all) {
    return all.entrySet().stream().filter(e -> e.getValue().isPresent()).collect(toMap(Map.Entry::getKey, e -> e
        .getValue().orElseThrow()));
  }

  <H> List<H> filterHoldingsBy(final List<H> holdings, final Predicate<H> predicate) {
    return holdings.stream().filter(predicate).toList();
  }

}
