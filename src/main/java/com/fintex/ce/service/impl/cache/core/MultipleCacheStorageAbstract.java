package com.fintex.ce.service.impl.cache.core;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.cache.CacheCategory;
import com.fintex.ce.config.enumeration.cache.CacheNameEntity;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.PagHolding;
import com.fintex.ce.dto.holding.SmaHolding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.core.MultipleSMRepository;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
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

@Log4j2
public abstract class MultipleCacheStorageAbstract<F extends RedisId, C extends RedisId, U extends RedisId, S extends RedisId>
        implements MultipleCacheStorage<F, C, U, S> {

    private final MultipleSMRepository<F, C, U, S> fdsRepo;

    final CoreRedisCacheRepository<F> fundCanadaCacheRepo;
    final CoreRedisCacheRepository<C> etfCanadaCacheRepo;
    final CoreRedisCacheRepository<U> etfUsCacheRepo;
    final CoreRedisCacheRepository<S> stockCacheRepo;
    final CoreRedisCacheRepository<F> usMutualFundRepo;
    final CoreRedisCacheRepository<F> canadaPooledFundRepo;
    final CoreRedisCacheRepository<F> canadaHedgeFundRepo;
    final CoreRedisCacheRepository<F> fixedIcomeCacheRepo;
    final CoreRedisCacheRepository<F> separatelyManagedAccountCacheRepo;
    final CoreRedisCacheRepository<F> pagGuidedPortfolioCacheRepo;

    private final CacheStatisticService cacheStatisticService;
    @Getter
    private final CacheNameEntity cacheNameEntity;

    public MultipleCacheStorageAbstract(MultipleSMRepository<F, C, U, S> fdsRepo,
                                        CoreRedisCacheRepository<F> fundCanadaCacheRepo,
                                        CoreRedisCacheRepository<C> etfCanadaCacheRepo,
                                        CoreRedisCacheRepository<U> etfUsCacheRepo,
                                        CoreRedisCacheRepository<S> stockCacheRepo,
                                        CacheStatisticService cacheStatisticService,
                                        CacheNameEntity cacheNameEntity) {
        this.fundCanadaCacheRepo = fundCanadaCacheRepo;
        this.etfCanadaCacheRepo = etfCanadaCacheRepo;
        this.etfUsCacheRepo = etfUsCacheRepo;
        this.stockCacheRepo = stockCacheRepo;
        this.usMutualFundRepo = fundCanadaCacheRepo; // this was done on purpose. MultipleCacheStorageAbstract has one major flow. It's accepting too many RedisCacheRepositories. Where in really almost all funds use the same RedisCacheRepository
        this.canadaPooledFundRepo = fundCanadaCacheRepo;
        this.canadaHedgeFundRepo = fundCanadaCacheRepo;
        this.fixedIcomeCacheRepo = fundCanadaCacheRepo;
        this.separatelyManagedAccountCacheRepo = fundCanadaCacheRepo;
        this.pagGuidedPortfolioCacheRepo = fundCanadaCacheRepo;


        this.fdsRepo = fdsRepo;
        this.cacheStatisticService = cacheStatisticService;
        this.cacheNameEntity = cacheNameEntity;
    }

    public abstract Object load(final List<Holding> holdings, final List<DataProvider> providers,
                                final List<Warning> warnings, final ParamHolderDTO paramHolderDTO);

    @Override
    public Map<StockHolding, S> loadForBenchOfStock(final List<StockHolding> holdings, final List<DataProvider> providers) {
        return synchronizeAndLoadForMultipleHoldings(holdings, providers, stockCacheRepo, fdsRepo::queryBenchOfStock, CacheCategory.STOCKS);
    }

    @Override
    public Map<FundSeriesHolding, F> loadBenchOfFundCanada(final List<FundSeriesHolding> holdings, final List<DataProvider> providers) {
        return synchronizeAndLoadForMultipleHoldings(holdings, providers, fundCanadaCacheRepo, fdsRepo::queryBenchOfFundCanada, CacheCategory.CANADA_MUTUAL_FUNDS);
    }

    @Override
    public Map<UsMutualFundHolding, F> loadUsMutualFunds(final List<UsMutualFundHolding> holdings, final List<DataProvider> providers) {
        return synchronizeAndLoadForMultipleHoldings(holdings, providers, usMutualFundRepo, fdsRepo::queryUsMutualFunds, CacheCategory.US_MUTUAL_FUNDS);
    }

    @Override
    public Map<CanadaPooledFundHolding, F> loadCanadaPooledFunds(final List<CanadaPooledFundHolding> holdings, final List<DataProvider> providers) {
        return synchronizeAndLoadForMultipleHoldings(holdings, providers, canadaPooledFundRepo, fdsRepo::queryCanadaPooledFunds, CacheCategory.CANADA_POOLED_FUNDS);
    }

    @Override
    public Map<CanadaHedgeFundHolding, F> loadCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings, final List<DataProvider> providers) {
        return synchronizeAndLoadForMultipleHoldings(holdings, providers, canadaHedgeFundRepo, fdsRepo::queryCanadaHedgeFunds, CacheCategory.CANADA_HEDGE_FUNDS);
    }

    @Override
    public Map<EtfHolding, C> loadForBenchOfEtfCanada(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return synchronizeAndLoadForMultipleHoldings(holdings, providers, etfCanadaCacheRepo, fdsRepo::queryBenchOfEtfCanada, CacheCategory.CANADA_ETF);
    }

    @Override
    public Map<EtfHolding, U> loadForBenchOfEtfUs(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return synchronizeAndLoadForMultipleHoldings(holdings, providers, etfUsCacheRepo, fdsRepo::queryBenchOfOfEtfUs, CacheCategory.US_ETF);
    }

    @Override
    public Map<FixedIncomeHolding, F> loadBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings, final List<DataProvider> providers) {
        return synchronizeAndLoadForMultipleHoldings(holdings, providers, fixedIcomeCacheRepo, fdsRepo::queryBenchOfFixedIncomes, CacheCategory.FIXED_INCOME);
    }

    @Override
    public Map<SmaHolding, F> loadBenchOfSeparatelyManagedAccounts(final List<SmaHolding> holdings, final List<DataProvider> providers) {
        return synchronizeAndLoadForMultipleHoldings(
                holdings,
                providers,
                separatelyManagedAccountCacheRepo,
                fdsRepo::queryBenchOfSeparatelyManagedAccounts,
                CacheCategory.SEPARATELY_MANAGED_ACCOUNT);
    }

    @Override
    public Map<PagHolding, F> loadBenchOfPagGuidedPortfolios(final List<PagHolding> holdings, final List<DataProvider> providers) {
        return synchronizeAndLoadForMultipleHoldings(
                holdings,
                providers,
                pagGuidedPortfolioCacheRepo,
                fdsRepo::queryBenchOfPagGuidedPortfolios,
                CacheCategory.PAG_GUIDED_PORTFOLIO);
    }

    /**
     * For benchmarks we use the same Redis Repository as for Mutual Funds
     *
     * @param holdings  holdings
     * @param providers providers
     * @return map as a response
     */
    @Override
    public Map<BenchmarkIndexHolding, F> loadForBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings, final List<DataProvider> providers) {
        return synchronizeAndLoadForMultipleHoldings(holdings, providers, fundCanadaCacheRepo, fdsRepo::queryBenchOfBenchmarks, CacheCategory.BENCHMARK_INDEXES);
    }

    /**
     * Caches the data for holdings and performs the actual FDS call if some holdings are not in the cache already
     *
     * @param holdings         all holdings
     * @param providers        user specified Data Providers
     * @param crudRepository   REDIS crud repository
     * @param benchFdsCallFunc FDS call
     * @param cacheCategory    cache category
     * @param <H>              holding type
     * @param <R>              response type
     * @return map of holding and their response type
     */
    <H extends Holding, R extends RedisId> Map<H, R> synchronizeAndLoadForMultipleHoldings(final List<H> holdings,
                                                                                           final List<DataProvider> providers,
                                                                                           final CoreRedisCacheRepository<R> crudRepository,
                                                                                           final BiFunction<List<H>, List<DataProvider>, Map<H, R>> benchFdsCallFunc,
                                                                                           final CacheCategory cacheCategory) {
        if (holdings.isEmpty()) {
            return new HashMap<>();
        }
        final Map<H, Optional<R>> all = loadCachesForHoldings(holdings, providers, crudRepository);
        final Map<H, R> cachedResponses = filterCachedResponses(all);
        final Map<H, R> preCachedResponses = cacheHoldings(holdings, providers, new ArrayList<>(cachedResponses.keySet()), benchFdsCallFunc, crudRepository);
        preCachedResponses.putAll(cachedResponses);
        CompletableFuture.runAsync(() -> cacheStatisticService.analyse(preCachedResponses, cacheNameEntity, cacheCategory));
        return preCachedResponses;
    }

    /**
     * Caches the data for holdings that are not in the cache for some of the holdings
     *
     * @param holdings         all holdings
     * @param providers        user specified Data Providers
     * @param cachedHoldings   already cached holdings
     * @param crudRepository   REDIS crud repository
     * @param benchFdsCallFunc FDS call
     * @param <H>              holding type
     * @param <R>              response type
     * @return map of holdings and their response types
     */
    <H, R extends RedisId> Map<H, R> cacheHoldings(final List<H> holdings,
                                                   final List<DataProvider> providers,
                                                   final List<H> cachedHoldings,
                                                   final BiFunction<List<H>, List<DataProvider>, Map<H, R>> benchFdsCallFunc,
                                                   final CoreRedisCacheRepository<R> crudRepository) {
        final List<H> uncachedHoldings = filterHoldingsBy(holdings, h -> !cachedHoldings.contains(h));
        final Map<H, R> uncachedHoldingResponses = uncachedHoldings.isEmpty() ? new HashMap<>() : benchFdsCallFunc.apply(uncachedHoldings, providers);
        // don't save to cache fds responses with errors.
        final HashMap<H, R> fdsResponseWithoutAnyErrors = filterResponsesWithoutAnyErrors(uncachedHoldingResponses);
        saveToCache(crudRepository, fdsResponseWithoutAnyErrors, providers);
        return uncachedHoldingResponses;
    }

    <H, R extends RedisId> HashMap<H, R> filterResponsesWithoutAnyErrors(Map<H, R> uncachedHoldingResponses) {
        return uncachedHoldingResponses.entrySet()
                .stream()
                .filter(e -> Objects.nonNull(e) && !e.getValue().hasErrors())
                .collect(toMap());
    }

    /**
     * Loads the data from the cache for holdings
     *
     * @param holdings       all holdings
     * @param providers      entered Data Providers
     * @param crudRepository Redis CRUD Repository
     * @param <H>            holding type
     * @param <R>            response type
     * @return map of holdings and their response types
     */
    <H extends Holding, R extends RedisId> Map<H, Optional<R>> loadCachesForHoldings(final List<H> holdings,
                                                                                     final List<DataProvider> providers,
                                                                                     final CoreRedisCacheRepository<R> crudRepository) {
        return holdings.stream().collect(toMap(e -> e, e -> queryCacheForHolding(e, providers, crudRepository)));
    }

    /**
     * Loads the data for a single holding from the cache
     *
     * @param h              holding
     * @param providers      entered Data Providers
     * @param crudRepository Redis CRUD Repository
     * @param <H>            holding type
     * @param <R>            response type
     * @return cached data for entered holding (if exists)
     */
    <H extends Holding, R extends RedisId> Optional<R> queryCacheForHolding(final H h,
                                                                            final List<DataProvider> providers,
                                                                            final CoreRedisCacheRepository<R> crudRepository) {
        if (!CollectionUtils.isEmpty(providers)) {
            return queryCacheForEnteredDataProviders(crudRepository, providers, h);
        }

        var start = System.currentTimeMillis();
        final List<R> all = crudRepository.findAllByHoldingId(h.generateUserIdentifier());
        log.debug("getting data from cache for {}, took {}ms", h.generateUserIdentifier(), System.currentTimeMillis() - start);
        if (all.isEmpty()) {
            return Optional.empty();
        }
        return pickUpProviderBasedOnPriority(all);
    }

    /**
     * Loads the response which has the highest data provider order
     *
     * @param all list of cached responses for the same holding id
     * @param <R> response type
     * @return single FDS response (if exists)
     */
    <R extends RedisId> Optional<R> pickUpProviderBasedOnPriority(final List<R> all) {
        for (DataProvider value : DataProvider.values()) {
            final Optional<R> first = all.stream().filter(r -> value.name().equalsIgnoreCase(r.getProvider())).findFirst();
            if (first.isPresent()) {
                return first;
            }
        }
        // or else return the first item from the list
        return Optional.of(all.get(0));
    }

    /**
     * Try to load response from the cache based on entered data providers
     *
     * @param crudRepository Redis CRUD Repository
     * @param providers      entered Data Providers
     * @param h              holding
     * @param <R>            response type
     * @param <H>            holding type
     * @return FDS response (if exists)
     */
    <R extends RedisId, H extends Holding> Optional<R> queryCacheForEnteredDataProviders(final CoreRedisCacheRepository<R> crudRepository,
                                                                                         final List<DataProvider> providers,
                                                                                         final H h) {
        final Optional<R> entity = crudRepository.findOneByHoldingIdAndProviders(h.generateUserIdentifier(), buildIdBasedOnProviders(providers));
        // if [MORNING_STAR, EAGLE] || [EAGLE, MORNING_STAR]
        if (entity.isEmpty() && providers.size() > 1) {
            // then query for MORNING_STAR
            return crudRepository.findOneByHoldingIdAndProvider(h.generateUserIdentifier(), providers.get(0).name());
        }
        return entity;
    }

    /**
     * Persists new responses to the cache
     *
     * @param <H>                      holding type
     * @param <R>                      response type
     * @param redisCrudRepository      Redis CRUD Repository
     * @param uncachedHoldingResponses uncached responses
     * @param providers                user providers
     */
    <H, R extends RedisId> void saveToCache(final CoreRedisCacheRepository<R> redisCrudRepository,
                                            final Map<H, R> uncachedHoldingResponses,
                                            final List<DataProvider> providers) {
        uncachedHoldingResponses.forEach((holding, response) -> {
            if (!CollectionUtils.isEmpty(providers)) {
                response.setProviders(buildIdBasedOnProviders(providers));
            }
            redisCrudRepository.save(response);
        });
    }

    <H, R> Map<H, R> filterCachedResponses(final Map<H, Optional<R>> all) {
        return all.entrySet().stream().filter(e -> e.getValue().isPresent()).collect(toMap(Map.Entry::getKey, e -> e.getValue().orElseThrow()));
    }

    <H> List<H> filterHoldingsBy(final List<H> holdings, final Predicate<H> predicate) {
        return holdings.stream().filter(predicate).toList();
    }

}
