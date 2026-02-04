package com.fintex.ce.adapter.cache.core;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.constant.CacheNameEntity;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.PagHolding;
import com.fintex.ce.domain.model.holding.SmaHolding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.cache.entity.core.RedisId;
import com.fintex.ce.port.output.cache.MultipleCacheStorage;
import com.fintex.ce.port.output.graphql.MultipleSMRepository;
import com.fintex.ce.port.mapper.CacheEntityMapper;
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
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.fintex.ce.util.CacheUtils.buildIdBasedOnProviders;
import static com.fintex.ce.util.CollectorUtils.toMap;

/**
 * Abstract base class for cache storage implementations. Works with domain models from SM repository and converts
 * to/from R* entities for Redis storage.
 *
 * @param <F>
 *          Fund Canada domain model type (also used for UsMutualFund, CanadaHedgeFund, Benchmark, etc.)
 * @param <C>
 *          ETF Canada domain model type
 * @param <U>
 *          ETF US domain model type
 * @param <S>
 *          Stock domain model type
 * @param <R>
 *          Redis entity type (extends RedisId, used for Redis storage)
 */
@Log4j2
public abstract class MultipleCacheStorageAbstract<F, C, U, S, R extends RedisId>
    implements
      MultipleCacheStorage<F, C, U, S> {

  private final MultipleSMRepository<F, C, U, S> smRepo;
  private final CacheEntityMapper<F, R> fundMapper;
  private final CacheEntityMapper<C, R> etfCanadaMapper;
  private final CacheEntityMapper<U, R> etfUsMapper;
  private final CacheEntityMapper<S, R> stockMapper;

  final CoreRedisCacheRepository<R> fundCanadaCacheRepo;
  final CoreRedisCacheRepository<R> etfCanadaCacheRepo;
  final CoreRedisCacheRepository<R> etfUsCacheRepo;
  final CoreRedisCacheRepository<R> stockCacheRepo;
  final CoreRedisCacheRepository<R> usMutualFundRepo;
  final CoreRedisCacheRepository<R> canadaPooledFundRepo;
  final CoreRedisCacheRepository<R> canadaHedgeFundRepo;
  final CoreRedisCacheRepository<R> fixedIcomeCacheRepo;
  final CoreRedisCacheRepository<R> separatelyManagedAccountCacheRepo;
  final CoreRedisCacheRepository<R> pagGuidedPortfolioCacheRepo;

  private final CacheStatisticService cacheStatisticService;
  @Getter
  private final CacheNameEntity cacheNameEntity;

  public MultipleCacheStorageAbstract(MultipleSMRepository<F, C, U, S> smRepo,
      CacheEntityMapper<F, R> fundMapper,
      CacheEntityMapper<C, R> etfCanadaMapper,
      CacheEntityMapper<U, R> etfUsMapper,
      CacheEntityMapper<S, R> stockMapper,
      CoreRedisCacheRepository<R> fundCanadaCacheRepo,
      CoreRedisCacheRepository<R> etfCanadaCacheRepo,
      CoreRedisCacheRepository<R> etfUsCacheRepo,
      CoreRedisCacheRepository<R> stockCacheRepo,
      CacheStatisticService cacheStatisticService,
      CacheNameEntity cacheNameEntity) {
    this.smRepo = smRepo;
    this.fundMapper = fundMapper;
    this.etfCanadaMapper = etfCanadaMapper;
    this.etfUsMapper = etfUsMapper;
    this.stockMapper = stockMapper;
    this.fundCanadaCacheRepo = fundCanadaCacheRepo;
    this.etfCanadaCacheRepo = etfCanadaCacheRepo;
    this.etfUsCacheRepo = etfUsCacheRepo;
    this.stockCacheRepo = stockCacheRepo;
    this.usMutualFundRepo = fundCanadaCacheRepo;
    this.canadaPooledFundRepo = fundCanadaCacheRepo;
    this.canadaHedgeFundRepo = fundCanadaCacheRepo;
    this.fixedIcomeCacheRepo = fundCanadaCacheRepo;
    this.separatelyManagedAccountCacheRepo = fundCanadaCacheRepo;
    this.pagGuidedPortfolioCacheRepo = fundCanadaCacheRepo;

    this.cacheStatisticService = cacheStatisticService;
    this.cacheNameEntity = cacheNameEntity;
  }

  public abstract Object load(final List<Holding> holdings, final List<DataProvider> providers,
      final List<Warning> warnings, final ParamHolderDTO paramHolderDTO);

  @Override
  public Map<StockHolding, S> loadForBenchOfStock(final List<StockHolding> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoadForMultipleHoldings(holdings, providers, stockCacheRepo, smRepo::queryBenchOfStock,
        stockMapper, CacheCategory.STOCKS);
  }

  @Override
  public Map<FundSeriesHolding, F> loadBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoadForMultipleHoldings(holdings, providers, fundCanadaCacheRepo,
        smRepo::queryBenchOfFundCanada, fundMapper, CacheCategory.CANADA_MUTUAL_FUNDS);
  }

  @Override
  public Map<UsMutualFundHolding, F> loadUsMutualFunds(final List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoadForMultipleHoldings(holdings, providers, usMutualFundRepo, smRepo::queryUsMutualFunds,
        fundMapper, CacheCategory.US_MUTUAL_FUNDS);
  }

  @Override
  public Map<CanadaPooledFundHolding, F> loadCanadaPooledFunds(final List<CanadaPooledFundHolding> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoadForMultipleHoldings(holdings, providers, canadaPooledFundRepo,
        smRepo::queryCanadaPooledFunds, fundMapper, CacheCategory.CANADA_POOLED_FUNDS);
  }

  @Override
  public Map<CanadaHedgeFundHolding, F> loadCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoadForMultipleHoldings(holdings, providers, canadaHedgeFundRepo,
        smRepo::queryCanadaHedgeFunds, fundMapper, CacheCategory.CANADA_HEDGE_FUNDS);
  }

  @Override
  public Map<EtfHolding, C> loadForBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoadForMultipleHoldings(holdings, providers, etfCanadaCacheRepo, smRepo::queryBenchOfEtfCanada,
        etfCanadaMapper, CacheCategory.CANADA_ETF);
  }

  @Override
  public Map<EtfHolding, U> loadForBenchOfEtfUs(final List<EtfHolding> holdings, final List<DataProvider> providers) {
    return synchronizeAndLoadForMultipleHoldings(holdings, providers, etfUsCacheRepo, smRepo::queryBenchOfOfEtfUs,
        etfUsMapper, CacheCategory.US_ETF);
  }

  @Override
  public Map<FixedIncomeHolding, F> loadBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoadForMultipleHoldings(holdings, providers, fixedIcomeCacheRepo,
        smRepo::queryBenchOfFixedIncomes, fundMapper, CacheCategory.FIXED_INCOME);
  }

  @Override
  public Map<SmaHolding, F> loadBenchOfSeparatelyManagedAccounts(final List<SmaHolding> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoadForMultipleHoldings(
        holdings,
        providers,
        separatelyManagedAccountCacheRepo,
        smRepo::queryBenchOfSeparatelyManagedAccounts,
        fundMapper,
        CacheCategory.SEPARATELY_MANAGED_ACCOUNT);
  }

  @Override
  public Map<PagHolding, F> loadBenchOfPagGuidedPortfolios(final List<PagHolding> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoadForMultipleHoldings(
        holdings,
        providers,
        pagGuidedPortfolioCacheRepo,
        smRepo::queryBenchOfPagGuidedPortfolios,
        fundMapper,
        CacheCategory.PAG_GUIDED_PORTFOLIO);
  }

  @Override
  public Map<BenchmarkIndexHolding, F> loadForBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings,
      final List<DataProvider> providers) {
    return synchronizeAndLoadForMultipleHoldings(holdings, providers, fundCanadaCacheRepo,
        smRepo::queryBenchOfBenchmarks, fundMapper, CacheCategory.BENCHMARK_INDEXES);
  }

  /**
   * Caches the data for holdings and performs the actual SM call if some holdings are not in the cache already. Handles
   * conversion between domain models and Redis entities.
   */
  public <H extends Holding, D> Map<H, D> synchronizeAndLoadForMultipleHoldings(
      final List<H> holdings,
      final List<DataProvider> providers,
      final CoreRedisCacheRepository<R> crudRepository,
      final BiFunction<List<H>, List<DataProvider>, Map<H, D>> smCallFunc,
      final CacheEntityMapper<D, R> mapper,
      final CacheCategory cacheCategory) {
    if (holdings.isEmpty()) {
      return new HashMap<>();
    }

    // Load from cache (as Redis entities) and convert to domain models
    final Map<H, Optional<R>> cachedRedisEntities = loadCachesForHoldings(holdings, providers, crudRepository);
    final Map<H, R> filteredCachedEntities = filterCachedResponses(cachedRedisEntities);
    final Map<H, D> cachedDomainModels = convertToDomainMap(filteredCachedEntities, mapper);

    // Fetch uncached holdings from SM repository (returns domain models)
    final Map<H, D> fetchedDomainModels = cacheHoldings(
        holdings,
        providers,
        new ArrayList<>(cachedDomainModels.keySet()),
        smCallFunc,
        crudRepository,
        mapper);

    // Merge cached and fetched results
    fetchedDomainModels.putAll(cachedDomainModels);

    // Analytics
    CompletableFuture.runAsync(() -> {
      Map<H, R> forAnalytics = convertToEntityMap(fetchedDomainModels, mapper);
      cacheStatisticService.analyse(forAnalytics, cacheNameEntity, cacheCategory);
    });

    return fetchedDomainModels;
  }

  /**
   * Caches the data for holdings that are not in the cache.
   */
  <H, D> Map<H, D> cacheHoldings(
      final List<H> holdings,
      final List<DataProvider> providers,
      final List<H> cachedHoldings,
      final BiFunction<List<H>, List<DataProvider>, Map<H, D>> smCallFunc,
      final CoreRedisCacheRepository<R> crudRepository,
      final CacheEntityMapper<D, R> mapper) {
    final List<H> uncachedHoldings = filterHoldingsBy(holdings, h -> !cachedHoldings.contains(h));
    final Map<H, D> uncachedDomainModels = uncachedHoldings.isEmpty()
        ? new HashMap<>()
        : smCallFunc.apply(uncachedHoldings, providers);

    // Convert to Redis entities and filter out responses with errors
    final Map<H, R> uncachedRedisEntities = convertToEntityMap(uncachedDomainModels, mapper);
    final HashMap<H, R> entitiesWithoutErrors = filterResponsesWithoutAnyErrors(uncachedRedisEntities);

    // Save to cache
    saveToCache(crudRepository, entitiesWithoutErrors, providers);

    return uncachedDomainModels;
  }

  /**
   * Convert a map of holdings to Redis entities into a map of holdings to domain models.
   */
  <H, D> Map<H, D> convertToDomainMap(Map<H, R> entityMap, CacheEntityMapper<D, R> mapper) {
    Map<H, D> result = new HashMap<>();
    entityMap.forEach((holding, entity) -> {
      mapper.toDomain(entity).ifPresent(domain -> result.put(holding, domain));
    });
    return result;
  }

  /**
   * Convert a map of holdings to domain models into a map of holdings to Redis entities.
   */
  <H, D> Map<H, R> convertToEntityMap(Map<H, D> domainMap, CacheEntityMapper<D, R> mapper) {
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
      final List<DataProvider> providers,
      final CoreRedisCacheRepository<R> crudRepository) {
    return holdings.stream().collect(toMap(e -> e, e -> queryCacheForHolding(e, providers, crudRepository)));
  }

  <H extends Holding> Optional<R> queryCacheForHolding(final H h,
      final List<DataProvider> providers,
      final CoreRedisCacheRepository<R> crudRepository) {
    if (!CollectionUtils.isEmpty(providers)) {
      return queryCacheForEnteredDataProviders(crudRepository, providers, h);
    }

    var start = System.currentTimeMillis();
    final List<R> all = crudRepository.findAllByHoldingId(h.generateUserIdentifier());
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

  <H extends Holding> Optional<R> queryCacheForEnteredDataProviders(final CoreRedisCacheRepository<R> crudRepository,
      final List<DataProvider> providers,
      final H h) {
    final Optional<R> entity = crudRepository.findOneByHoldingIdAndProviders(h.generateUserIdentifier(),
        buildIdBasedOnProviders(providers));
    if (entity.isEmpty() && providers.size() > 1) {
      return crudRepository.findOneByHoldingIdAndProvider(h.generateUserIdentifier(), providers.get(0).name());
    }
    return entity;
  }

  <H> void saveToCache(final CoreRedisCacheRepository<R> redisCrudRepository,
      final Map<H, R> uncachedHoldingResponses,
      final List<DataProvider> providers) {
    uncachedHoldingResponses.forEach((holding, response) -> {
      if (!CollectionUtils.isEmpty(providers)) {
        response.setProviders(buildIdBasedOnProviders(providers));
      }
      redisCrudRepository.save(response);
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
