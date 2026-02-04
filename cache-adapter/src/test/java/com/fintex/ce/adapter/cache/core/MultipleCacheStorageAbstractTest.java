package com.fintex.ce.adapter.cache.core;

import com.fintex.ce.adapter.cache.entity.core.RedisId;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticService;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.SmaHolding;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.graphql.MultipleSMRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.fintex.ce.domain.enumeration.DataProvider.EAGLE;
import static com.fintex.ce.domain.enumeration.DataProvider.MORNINGSTAR;
import static com.fintex.ce.domain.enumeration.HoldingType.CASH;
import static com.fintex.ce.domain.enumeration.HoldingType.US_ETF;
import static com.fintex.ce.util.CacheUtils.buildIdBasedOnProviders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class MultipleCacheStorageAbstractTest {

  @Test
  void synchronizeAndLoadForMultipleHoldings_verifyLoadCachesForHoldings() {
    // SETUP
    final CacheStatisticService cacheS = mock(CacheStatisticService.class);
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class,
        withSettings().useConstructor(null, null, null, null, null, null, null, null, null, cacheS, null));

    final List<Holding> holdings = List.of(mock(Holding.class));

    doCallRealMethod().when(m).synchronizeAndLoadForMultipleHoldings(any(), any(), any(), any(), any(), any());
    // ACT
    final List<DataProvider> providers = List.of(EAGLE);
    final CoreRedisCacheRepository cacheRepository = mock(CoreRedisCacheRepository.class);
    m.synchronizeAndLoadForMultipleHoldings(holdings, providers, cacheRepository, null, null, null);

    // VERIFY
    verify(m).loadCachesForHoldings(holdings, providers, cacheRepository);
  }

  @Test
  void synchronizeAndLoadForMultipleHoldings_verifyGetCachedResponses() {
    // SETUP
    final CacheStatisticService cacheS = mock(CacheStatisticService.class);
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class,
        withSettings().useConstructor(null, null, null, null, null, null, null, null, null, cacheS, null));

    final List<Object> holdings = List.of(mock(Holding.class));
    final HashMap map = new HashMap();
    when(m.loadCachesForHoldings(any(), any(), any())).thenReturn(map);

    doCallRealMethod().when(m).synchronizeAndLoadForMultipleHoldings(any(), any(), any(), any(), any(), any());
    // ACT
    final List<DataProvider> providers = List.of(EAGLE);
    final CoreRedisCacheRepository cacheRepository = mock(CoreRedisCacheRepository.class);
    m.synchronizeAndLoadForMultipleHoldings(holdings, providers, cacheRepository, null, null, null);

    // VERIFY
    verify(m).filterCachedResponses(argThat(arg -> arg == map));
  }

  @Test
  void synchronizeAndLoadForMultipleHoldings_verifyCacheHoldings() {
    // SETUP
    final CacheStatisticService cacheS = mock(CacheStatisticService.class);
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class,
        withSettings().useConstructor(null, null, null, null, null, null, null, null, null, cacheS, null));

    final List<Object> holdings = List.of(mock(Holding.class));

    final Holding h = mock(Holding.class);
    when(m.filterCachedResponses(any())).thenReturn(Map.of(h, mock(RedisId.class)));
    when(m.convertToDomainMap(any(), any())).thenReturn(Map.of(h, new Object()));

    doCallRealMethod().when(m).synchronizeAndLoadForMultipleHoldings(any(), any(), any(), any(), any(), any());
    // ACT
    final List<DataProvider> providers = List.of(EAGLE);
    final CoreRedisCacheRepository cacheRepository = mock(CoreRedisCacheRepository.class);
    final BiFunction biFunction = mock(BiFunction.class);
    final CacheEntityMapper mapper = mock(CacheEntityMapper.class);
    m.synchronizeAndLoadForMultipleHoldings(holdings, providers, cacheRepository, biFunction, mapper, null);

    // VERIFY
    verify(m).cacheHoldings(argThat(arg -> arg == holdings), eq(providers), eq(List.of(h)), eq(biFunction), eq(
        cacheRepository), eq(mapper));
  }

  // @Test
  // void synchronizeAndLoadForMultipleHoldings_verifyAnalyse() {
  // //SETUP
  // final FASUsageStatisticsRepo fasUsageStatisticsRepo = mock(FASUsageStatisticsRepo.class);
  // final CacheStatisticServiceImpl cacheS = mock(CacheStatisticServiceImpl.class,
  // withSettings().useConstructor(fasUsageStatisticsRepo));
  // final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class,
  // withSettings().useConstructor(null, null, null, null, null, cacheS, CacheNameEntity.MER));
  //
  // final List<Holding> holdings = List.of(mock(Holding.class));
  //
  // final HashMap all = new HashMap();
  // when(m.cacheHoldings(any(), any(), any(), any(), any(), any())).thenReturn(all);
  //
  // doCallRealMethod().when(m).synchronizeAndLoadForMultipleHoldings(any(), any(), any(), any(), any(), any());
  // //ACT
  // final List<DataProvider> providers = List.of(EAGLE);
  // final CoreRedisCacheRepository cacheRepository = mock(CoreRedisCacheRepository.class);
  // m.synchronizeAndLoadForMultipleHoldings(holdings, providers, cacheRepository, null, null, CacheCategory.STOCKS);
  //
  // //VERIFY
  // verify(cacheS).analyse(argThat(arg -> arg == all), eq(CacheNameEntity.MER), eq(CacheCategory.STOCKS));
  //
  // }

  @Test
  void synchronizeAndLoadForMultipleHoldings_checkResults() {
    // SETUP
    final CacheStatisticService cacheS = mock(CacheStatisticService.class);
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class,
        withSettings().useConstructor(null, null, null, null, null, null, null, null, null, cacheS, null));

    final HashMap cachedDomainModels = new HashMap();
    cachedDomainModels.put("1", "1");
    final HashMap fetchedDomainModels = new HashMap();
    fetchedDomainModels.put("12", "12");

    when(m.filterCachedResponses(any())).thenReturn(new HashMap<>());
    when(m.convertToDomainMap(any(), any())).thenReturn(cachedDomainModels);
    when(m.cacheHoldings(any(), any(), any(), any(), any(), any())).thenReturn(fetchedDomainModels);

    doCallRealMethod().when(m).synchronizeAndLoadForMultipleHoldings(any(), any(), any(), any(), any(), any());
    // ACT
    final Map actual = m.synchronizeAndLoadForMultipleHoldings(List.of(mock(Holding.class)), List.of(), null, null,
        null, CacheCategory.STOCKS);

    // VERIFY
    assertEquals(Map.of("1", "1", "12", "12"), actual);
  }

  @Test
  void synchronizeAndLoadForMultipleHoldings_checkResult_whenHoldingsIsEmpty() {
    // SETUP
    final var sut = mock(MultipleCacheStorageAbstract.class);
    final var holdings = mock(List.class);

    when(holdings.isEmpty()).thenReturn(true);
    doCallRealMethod().when(sut).synchronizeAndLoadForMultipleHoldings(any(), any(), any(), any(), any(), any());

    // ACT
    final Map actual = sut.synchronizeAndLoadForMultipleHoldings(holdings, mock(List.class),
        mock(CoreRedisCacheRepository.class), mock(BiFunction.class), mock(CacheEntityMapper.class), CacheCategory.ETF);

    // VERIFY
    assertTrue(actual.isEmpty());
    verify(sut).synchronizeAndLoadForMultipleHoldings(same(holdings), any(), any(), any(), any(), any());
    verifyNoMoreInteractions(sut);
  }

  @Test
  void cacheHoldings_verifyFilterHoldingsBy() {
    // SETUP
    final CacheStatisticService cacheS = mock(CacheStatisticService.class);
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class,
        withSettings().useConstructor(null, null, null, null, null, null, null, null, null, cacheS, null));

    final List<Object> holdings = new ArrayList<>();

    doCallRealMethod().when(m).cacheHoldings(any(), any(), any(), any(), any(), any());
    // ACT
    m.cacheHoldings(holdings, null, List.of(), null, null, null);

    // VERIFY
    verify(m).filterHoldingsBy(argThat(h -> h == holdings), any());
  }

  @Test
  void cacheHoldings_verifyFunctionInvocation() {
    // SETUP
    final MultipleCacheStorageAbstract sut = mock(MultipleCacheStorageAbstract.class);

    final BiFunction fdsCall = mock(BiFunction.class);
    final List<Object> holdings = List.of(mock(Holding.class));

    when(sut.filterHoldingsBy(any(), any())).thenReturn(holdings);

    doCallRealMethod().when(sut).cacheHoldings(any(), any(), any(), any(), any(), any());
    // ACT
    final List<DataProvider> providers = List.of(EAGLE);
    sut.cacheHoldings(null, providers, null, fdsCall, null, null);

    // VERIFY
    verify(fdsCall).apply(holdings, providers);
  }

  @Test
  void cacheHoldings_verifySaveToCacheIsEmpty() {
    // SETUP
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class);

    final BiFunction fdsCall = mock(BiFunction.class);

    final Map<Holding, Object> map = Map.of(mock(Holding.class), new Object());
    when(fdsCall.apply(any(), any())).thenReturn(map);
    when(m.filterHoldingsBy(any(), any())).thenReturn(List.of());

    doCallRealMethod().when(m).cacheHoldings(any(), any(), any(), any(), any(), any());
    // ACT
    final CoreRedisCacheRepository cacheRepository = mock(CoreRedisCacheRepository.class);
    m.cacheHoldings(null, List.of(EAGLE), List.of(), fdsCall, cacheRepository, null);

    // VERIFY
    verify(m).saveToCache(argThat(arg -> arg == cacheRepository), argThat(Map::isEmpty), eq(List.of(EAGLE)));
  }

  @Test
  void cacheHoldings_verifySaveToCacheNotEmpty() {
    // SETUP
    final MultipleCacheStorageAbstract sut = mock(MultipleCacheStorageAbstract.class);

    final BiFunction fdsCall = mock(BiFunction.class);

    final Map<Holding, Object> allResponses = Map.of(mock(Holding.class), new Object());
    final HashMap<Holding, RedisId> responsesWithoutAnyErrors = new HashMap<>(Map.of(mock(Holding.class),
        mock(RedisId.class)));

    when(sut.filterResponsesWithoutAnyErrors(any())).thenReturn(responsesWithoutAnyErrors);
    when(fdsCall.apply(any(), any())).thenReturn(allResponses);
    when(sut.filterHoldingsBy(any(), any())).thenReturn(List.of(mock(Holding.class)));

    doCallRealMethod().when(sut).cacheHoldings(any(), any(), any(), any(), any(), any());
    // ACT
    final CoreRedisCacheRepository cacheRepository = mock(CoreRedisCacheRepository.class);
    sut.cacheHoldings(null, List.of(EAGLE), List.of(), fdsCall, cacheRepository, null);

    // VERIFY
    verify(sut).saveToCache(argThat(arg -> arg == cacheRepository), argThat(arg -> arg == responsesWithoutAnyErrors),
        eq(List.of(EAGLE)));
  }

  @Test
  void cacheHoldings_resultList() {
    // SETUP
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class);

    final BiFunction fdsCall = mock(BiFunction.class);
    final Map<Holding, Object> map = Map.of(mock(Holding.class), new Object());
    when(fdsCall.apply(any(), any())).thenReturn(map);
    when(m.filterHoldingsBy(any(), any())).thenReturn(List.of(mock(Holding.class)));

    doCallRealMethod().when(m).cacheHoldings(any(), any(), any(), any(), any(), any());
    // ACT
    final CoreRedisCacheRepository cacheRepository = mock(CoreRedisCacheRepository.class);
    final Map actual = m.cacheHoldings(null, null, List.of(), fdsCall, cacheRepository, null);

    // VERIFY
    assertEquals(map, actual);
  }

  @Test
  void saveToCache_verifySave() {
    // SETUP
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class);

    final CoreRedisCacheRepository repo = mock(CoreRedisCacheRepository.class);
    when(repo.findOneByHoldingIdAndProvider(any(), any())).thenReturn(Optional.empty());

    final DataProvider eagle = EAGLE;
    final String providersId = buildIdBasedOnProviders(List.of(eagle));

    final RedisId res = mock(RedisId.class);
    when(res.getProvider()).thenReturn(eagle.name());
    when(res.getProviders()).thenReturn(providersId);
    when(res.getHoldingId()).thenReturn("ID");
    final Map<Object, Object> map = Map.of(mock(Holding.class), res);

    doCallRealMethod().when(m).saveToCache(any(), anyMap(), any());
    // ACT
    m.saveToCache(repo, map, List.of(eagle));

    // VERIFY
    verify(repo).save(res);
  }

  @Test
  void saveToCache_verifySaveWhenProvidersAreAllNull() {
    // SETUP
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class);

    final CoreRedisCacheRepository repo = mock(CoreRedisCacheRepository.class);
    when(repo.findOneByHoldingIdAndProvider(any(), any())).thenReturn(Optional.empty());

    final RedisId res = mock(RedisId.class);
    when(res.getProvider()).thenReturn("");
    when(res.getProviders()).thenReturn("");
    when(res.getHoldingId()).thenReturn("ID");

    final Map<Object, Object> map = Map.of(mock(Holding.class), res);

    doCallRealMethod().when(m).saveToCache(any(), anyMap(), any());
    // ACT
    m.saveToCache(repo, map, null);

    // VERIFY
    verify(repo).save(res);
  }

  @Test
  void saveToCache_verifySaveWhenProvidersAreEmpty() {
    // SETUP
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class);

    final CoreRedisCacheRepository repo = mock(CoreRedisCacheRepository.class);
    when(repo.findOneByHoldingIdAndProvider(any(), any())).thenReturn(Optional.empty());

    final RedisId res = mock(RedisId.class);
    when(res.getProvider()).thenReturn("");
    when(res.getProviders()).thenReturn("");
    when(res.getHoldingId()).thenReturn("ID");

    final Map<Object, Object> map = Map.of(mock(Holding.class), res);

    doCallRealMethod().when(m).saveToCache(any(), anyMap(), any());
    // ACT
    m.saveToCache(repo, map, List.of());

    // VERIFY
    verify(repo).save(res);
  }

  @Test
  void getCachedResponses_verify() {
    // SETUP
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class);

    final Holding h = new Holding().setType(CASH);
    final Optional<RedisId> res = Optional.of(mock(RedisId.class));

    final Map<Holding, Optional<RedisId>> of = Map.of(h, res, mock(Holding.class), Optional.empty());

    doCallRealMethod().when(m).filterCachedResponses(anyMap());
    // ACT
    final Map actual = m.filterCachedResponses(of);

    // VERIFY
    assertEquals(Map.of(h, res.orElseThrow()), actual);
  }

  @Test
  void filterHoldingsBy_verify() {
    // SETUP
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class);

    final Holding h = new Holding().setType(CASH);
    final List<Holding> holdings = List.of(h, new Holding().setType(US_ETF));

    doCallRealMethod().when(m).filterHoldingsBy(any(), any());
    // ACT
    final List set = m.filterHoldingsBy(holdings, o -> ((Holding) o).getType() == CASH);

    // VERIFY
    assertEquals(List.of(h), set);
  }

  @Test
  void loadBenchOfFundCanada_resultList() {
    // SETUP
    final CoreRedisCacheRepository fundCanada = mock(CoreRedisCacheRepository.class);
    final MultipleSMRepository fdsRepo = mock(MultipleSMRepository.class);
    final CacheStatisticService cacheS = mock(CacheStatisticService.class);
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class,
        withSettings().useConstructor(fdsRepo, null, null, null, null, fundCanada, null, null, null, cacheS, null));

    final FundSeriesHolding h = mock(FundSeriesHolding.class);

    final List<FundSeriesHolding> holdings = List.of(h);

    final Map<Object, Object> map = Map.of();
    when(fdsRepo.queryBenchOfFundCanada(any(), anyList())).thenReturn(map);

    final HashMap expected = new HashMap();
    when(m.synchronizeAndLoadForMultipleHoldings(any(), any(), any(), any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(m).loadBenchOfFundCanada(any(), anyList());
    // ACT
    final List<DataProvider> providers = List.of(EAGLE);
    final Map actual = m.loadBenchOfFundCanada(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
    verify(m).synchronizeAndLoadForMultipleHoldings(
        eq(holdings), eq(providers), eq(fundCanada), argThat(arg -> arg.apply(null, providers) == map), any(), eq(
            CacheCategory.CANADA_MUTUAL_FUNDS));
  }

  @Test
  void loadBenchOfFixedIncome_resultList() {
    // SETUP
    final CoreRedisCacheRepository fixedIncomeCacheRepository = mock(CoreRedisCacheRepository.class);
    final MultipleSMRepository fdsRepo = mock(MultipleSMRepository.class);
    final CacheStatisticService cacheS = mock(CacheStatisticService.class);
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class,
        withSettings().useConstructor(fdsRepo, null, null, null, null, fixedIncomeCacheRepository, null, null, null, cacheS, null));

    final FixedIncomeHolding h = mock(FixedIncomeHolding.class);

    final List<FixedIncomeHolding> holdings = List.of(h);

    final Map<Object, Object> map = Map.of();
    when(fdsRepo.queryBenchOfFixedIncomes(any(), anyList())).thenReturn(map);

    final HashMap expected = new HashMap();
    when(m.synchronizeAndLoadForMultipleHoldings(any(), any(), any(), any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(m).loadBenchOfFixedIncomes(any(), anyList());
    // ACT
    final List<DataProvider> providers = List.of(EAGLE);
    final Map actual = m.loadBenchOfFixedIncomes(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
    verify(m).synchronizeAndLoadForMultipleHoldings(
        eq(holdings), eq(providers), eq(fixedIncomeCacheRepository), argThat(arg -> arg.apply(null, providers) == map),
        any(), eq(CacheCategory.FIXED_INCOME));
  }

  @Test
  void loadBenchOfSeparatelyManaged_resultList() {
    // SETUP
    final CoreRedisCacheRepository smaCacheRepository = mock(CoreRedisCacheRepository.class);
    final MultipleSMRepository fdsRepo = mock(MultipleSMRepository.class);
    final CacheStatisticService cacheS = mock(CacheStatisticService.class);
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class,
        withSettings().useConstructor(fdsRepo, null, null, null, null, smaCacheRepository, null, null, null, cacheS, null));

    final SmaHolding h = mock(SmaHolding.class);

    final List<SmaHolding> holdings = List.of(h);

    final Map<Object, Object> map = Map.of();
    when(fdsRepo.queryBenchOfSeparatelyManagedAccounts(any(), anyList())).thenReturn(map);

    final HashMap expected = new HashMap();
    when(m.synchronizeAndLoadForMultipleHoldings(any(), any(), any(), any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(m).loadBenchOfSeparatelyManagedAccounts(any(), anyList());
    // ACT
    final List<DataProvider> providers = List.of(EAGLE);
    final Map actual = m.loadBenchOfSeparatelyManagedAccounts(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
    verify(m).synchronizeAndLoadForMultipleHoldings(
        eq(holdings), eq(providers), eq(smaCacheRepository),
        argThat(arg -> arg.apply(null, providers) == map), any(), eq(CacheCategory.SEPARATELY_MANAGED_ACCOUNT));
  }

  @Test
  void loadForBenchOfStock_resultList() {
    // SETUP
    final CoreRedisCacheRepository repo = mock(CoreRedisCacheRepository.class);
    final MultipleSMRepository fdsRepo = mock(MultipleSMRepository.class);
    final CacheStatisticService cacheS = mock(CacheStatisticService.class);
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class,
        withSettings().useConstructor(fdsRepo, null, null, null, null, null, null, null, repo, cacheS, null));

    final FundSeriesHolding h = mock(FundSeriesHolding.class);

    final List<FundSeriesHolding> holdings = List.of(h);

    final Map<Object, Object> map = Map.of();
    when(fdsRepo.queryBenchOfStock(any(), anyList())).thenReturn(map);

    final HashMap expected = new HashMap();
    when(m.synchronizeAndLoadForMultipleHoldings(any(), any(), any(), any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(m).loadForBenchOfStock(any(), anyList());
    // ACT
    final List<DataProvider> providers = List.of(EAGLE);
    final Map actual = m.loadForBenchOfStock(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
    verify(m).synchronizeAndLoadForMultipleHoldings(
        eq(holdings), eq(providers), eq(repo), argThat(arg -> arg.apply(null, providers) == map), any(), eq(
            CacheCategory.STOCKS));
  }

  @Test
  void loadForBenchOfEtfCanada_resultList() {
    // SETUP
    final CoreRedisCacheRepository repo = mock(CoreRedisCacheRepository.class);
    final MultipleSMRepository fdsRepo = mock(MultipleSMRepository.class);
    final CacheStatisticService cacheS = mock(CacheStatisticService.class);
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class,
        withSettings().useConstructor(fdsRepo, null, null, null, null, null, repo, null, null, cacheS, null));

    final FundSeriesHolding h = mock(FundSeriesHolding.class);

    final List<FundSeriesHolding> holdings = List.of(h);

    final Map<Object, Object> map = Map.of();
    when(fdsRepo.queryBenchOfEtfCanada(any(), anyList())).thenReturn(map);

    final HashMap expected = new HashMap();
    when(m.synchronizeAndLoadForMultipleHoldings(any(), any(), any(), any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(m).loadForBenchOfEtfCanada(any(), anyList());
    // ACT
    final List<DataProvider> providers = List.of(EAGLE);
    final Map actual = m.loadForBenchOfEtfCanada(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
    verify(m).synchronizeAndLoadForMultipleHoldings(
        eq(holdings), eq(providers), eq(repo), argThat(arg -> arg.apply(null, providers) == map), any(), eq(
            CacheCategory.CANADA_ETF));
  }

  @Test
  void loadForBenchOfEtfUs_resultList() {
    // SETUP
    final CoreRedisCacheRepository repo = mock(CoreRedisCacheRepository.class);
    final MultipleSMRepository fdsRepo = mock(MultipleSMRepository.class);
    final CacheStatisticService cacheS = mock(CacheStatisticService.class);
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class,
        withSettings().useConstructor(fdsRepo, null, null, null, null, null, null, repo, null, cacheS, null));

    final FundSeriesHolding h = mock(FundSeriesHolding.class);

    final List<FundSeriesHolding> holdings = List.of(h);

    final Map<Object, Object> map = Map.of();
    when(fdsRepo.queryBenchOfOfEtfUs(any(), anyList())).thenReturn(map);

    final HashMap expected = new HashMap();
    when(m.synchronizeAndLoadForMultipleHoldings(any(), any(), any(), any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(m).loadForBenchOfEtfUs(any(), anyList());
    // ACT
    final List<DataProvider> providers = List.of(EAGLE);
    final Map actual = m.loadForBenchOfEtfUs(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
    verify(m).synchronizeAndLoadForMultipleHoldings(
        eq(holdings), eq(providers), eq(repo), argThat(arg -> arg.apply(null, providers) == map), any(), eq(
            CacheCategory.US_ETF));
  }

  @Test
  void loadForBenchOfBenchmarks_resultList() {
    // SETUP
    final CoreRedisCacheRepository repo = mock(CoreRedisCacheRepository.class);
    final MultipleSMRepository fdsRepo = mock(MultipleSMRepository.class);
    final CacheStatisticService cacheS = mock(CacheStatisticService.class);
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class,
        withSettings().useConstructor(fdsRepo, null, null, null, null, repo, null, null, null, cacheS, null));

    final BenchmarkIndexHolding h = mock(BenchmarkIndexHolding.class);
    final List<BenchmarkIndexHolding> holdings = List.of(h);

    final Map<Object, Object> map = Map.of();
    when(fdsRepo.queryBenchOfBenchmarks(any(), anyList())).thenReturn(map);

    final HashMap expected = new HashMap();
    when(m.synchronizeAndLoadForMultipleHoldings(any(), any(), any(), any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(m).loadForBenchOfBenchmarks(any(), anyList());
    // ACT
    final List<DataProvider> providers = List.of(EAGLE);
    final Map actual = m.loadForBenchOfBenchmarks(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
    verify(m).synchronizeAndLoadForMultipleHoldings(
        eq(holdings), eq(providers), eq(repo), argThat(arg -> arg.apply(null, providers) == map), any(), eq(
            CacheCategory.BENCHMARK_INDEXES));
  }

  @Test
  void queryCacheForEnteredDataProviders_notExists() {
    // SETUP
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class);

    final CoreRedisCacheRepository repo = mock(CoreRedisCacheRepository.class);
    when(repo.findOneByHoldingIdAndProvider(any(), any())).thenReturn(Optional.empty());

    final Holding h = mock(Holding.class);
    when(h.generateUserIdentifier()).thenReturn("ID");

    final List<DataProvider> eagle = List.of(EAGLE);

    doCallRealMethod().when(m).queryCacheForEnteredDataProviders(any(), any(), any());
    // ACT
    final Optional actual = m.queryCacheForEnteredDataProviders(repo, eagle, h);

    // VERIFY
    verify(repo).findOneByHoldingIdAndProviders(h.generateUserIdentifier(), buildIdBasedOnProviders(eagle));
    assertEquals(Optional.empty(), actual);
  }

  @Test
  void queryCacheForEnteredDataProviders_exists() {
    // SETUP
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class);

    final CoreRedisCacheRepository repo = mock(CoreRedisCacheRepository.class);
    final RedisId res = mock(RedisId.class);
    when(repo.findOneByHoldingIdAndProviders(any(), any())).thenReturn(Optional.of(res));

    final Holding h = mock(Holding.class);
    when(h.generateUserIdentifier()).thenReturn("ID");

    final List<DataProvider> eagle = List.of(EAGLE);

    doCallRealMethod().when(m).queryCacheForEnteredDataProviders(any(), any(), any());
    // ACT
    final Optional actual = m.queryCacheForEnteredDataProviders(repo, eagle, h);

    // VERIFY
    verify(repo).findOneByHoldingIdAndProviders(h.generateUserIdentifier(), buildIdBasedOnProviders(eagle));
    verify(repo, times(0)).findOneByHoldingIdAndProvider(h.generateUserIdentifier(), eagle.get(0).name());
    assertEquals(Optional.of(res), actual);
  }

  @Test
  void queryCacheForEnteredDataProviders_exists2() {
    // SETUP
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class);

    final CoreRedisCacheRepository repo = mock(CoreRedisCacheRepository.class);
    final RedisId res = mock(RedisId.class);

    when(repo.findOneByHoldingIdAndProviders(any(), any())).thenReturn(Optional.empty());
    when(repo.findOneByHoldingIdAndProvider(any(), any())).thenReturn(Optional.of(res));

    final Holding h = mock(Holding.class);
    when(h.generateUserIdentifier()).thenReturn("ID");

    final List<DataProvider> providers = List.of(EAGLE, MORNINGSTAR);
    final String providersId = buildIdBasedOnProviders(providers);

    doCallRealMethod().when(m).queryCacheForEnteredDataProviders(any(), any(), any());
    // ACT
    final Optional actual = m.queryCacheForEnteredDataProviders(repo, providers, h);

    // VERIFY
    verify(repo).findOneByHoldingIdAndProvider(h.generateUserIdentifier(), providers.get(0).name());
    verify(repo, times(0)).findOneByHoldingIdAndProvider(h.generateUserIdentifier(), providers.get(1).name());
    assertEquals(Optional.of(res), actual);
  }

  @Test
  void pickUpProviderBasedOnPriority_checkResult() {
    // SETUP
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class);

    final RedisId res = mock(RedisId.class);
    when(res.getProvider()).thenReturn(EAGLE.name());
    final RedisId res2 = mock(RedisId.class);
    when(res2.getProvider()).thenReturn("");

    doCallRealMethod().when(m).pickUpProviderBasedOnPriority(any());
    // ACT
    final Optional actual = m.pickUpProviderBasedOnPriority(List.of(res, res2));

    // VERIFY
    assertEquals(Optional.of(res), actual);
  }

  @Test
  void pickUpProviderBasedOnPriority_checkResult2() {
    // SETUP
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class);

    final RedisId res2 = mock(RedisId.class);
    when(res2.getProvider()).thenReturn("");

    doCallRealMethod().when(m).pickUpProviderBasedOnPriority(any());
    // ACT
    final Optional actual = m.pickUpProviderBasedOnPriority(List.of(res2));

    // VERIFY
    assertEquals(Optional.of(res2), actual);
  }

  @Test
  void queryCacheForHolding_verifyQueryCacheForEnteredDataProviders() {
    // SETUP
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class);

    final List<DataProvider> eagle = List.of(EAGLE);
    final CoreRedisCacheRepository repo = mock(CoreRedisCacheRepository.class);
    final Holding h = mock(Holding.class);

    doCallRealMethod().when(m).queryCacheForHolding(any(), any(), any());
    // ACT
    m.queryCacheForHolding(h, eagle, repo);

    // VERIFY
    verify(m).queryCacheForEnteredDataProviders(repo, eagle, h);
  }

  @Test
  void loadCachesForHoldings_checkResult() {
    // SETUP
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class);

    final List<DataProvider> eagle = List.of(EAGLE);
    final CoreRedisCacheRepository repo = mock(CoreRedisCacheRepository.class);

    final Holding h = mock(Holding.class);

    when(m.queryCacheForHolding(any(), any(), any())).thenReturn(Optional.empty());

    doCallRealMethod().when(m).loadCachesForHoldings(any(), any(), any());
    // ACT
    final Map map = m.loadCachesForHoldings(List.of(h), eagle, repo);

    // VERIFY
    assertEquals(Map.of(h, Optional.empty()), map);
  }

  @Test
  void queryCacheForHolding_verifyFindAllByHoldingIdEmpty() {
    // SETUP
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class);

    final List<DataProvider> eagle = List.of();
    final CoreRedisCacheRepository repo = mock(CoreRedisCacheRepository.class);

    final Holding h = mock(Holding.class);
    when(h.generateUserIdentifier()).thenReturn("SDF");

    when(repo.findAllByHoldingId(any())).thenReturn(List.of());

    doCallRealMethod().when(m).queryCacheForHolding(any(), any(), any());
    // ACT
    final Optional actual = m.queryCacheForHolding(h, eagle, repo);

    // VERIFY
    verify(repo).findAllByHoldingId(h.generateUserIdentifier());
    assertEquals(Optional.empty(), actual);
  }

  @Test
  void queryCacheForHolding_verifyPickUpProviderBasedOnPriority() {
    // SETUP
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class);

    final List<DataProvider> eagle = List.of();
    final CoreRedisCacheRepository repo = mock(CoreRedisCacheRepository.class);

    final Holding h = mock(Holding.class);
    when(h.generateUserIdentifier()).thenReturn("SDF");

    final List<Holding> all = List.of(mock(Holding.class));
    when(repo.findAllByHoldingId(any())).thenReturn(all);

    doCallRealMethod().when(m).queryCacheForHolding(any(), any(), any());
    // ACT
    final Optional actual = m.queryCacheForHolding(h, eagle, repo);

    // VERIFY
    verify(m).pickUpProviderBasedOnPriority(all);
  }

  @Test
  void queryCacheForHolding_checkResult() {
    // SETUP
    final MultipleCacheStorageAbstract m = mock(MultipleCacheStorageAbstract.class);

    final List<DataProvider> eagle = List.of();
    final CoreRedisCacheRepository repo = mock(CoreRedisCacheRepository.class);

    final Holding h = mock(Holding.class);
    when(h.generateUserIdentifier()).thenReturn("SDF");

    final List<Holding> all = List.of(mock(Holding.class));
    when(repo.findAllByHoldingId(any())).thenReturn(all);

    final Optional<Object> expected = Optional.of(mock(Object.class));
    when(m.pickUpProviderBasedOnPriority(any())).thenReturn(expected);

    doCallRealMethod().when(m).queryCacheForHolding(any(), any(), any());
    // ACT
    final Optional actual = m.queryCacheForHolding(h, eagle, repo);

    // VERIFY
    assertSame(expected, actual);
  }

}