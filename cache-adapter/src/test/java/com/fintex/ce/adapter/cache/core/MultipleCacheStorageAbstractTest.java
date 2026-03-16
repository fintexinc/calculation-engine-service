package com.fintex.ce.adapter.cache.core;

import com.fintex.ce.adapter.cache.entity.core.RedisId;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings({"unchecked", "rawtypes"})
class CacheStorageAbstractTest {

  private static int holdingCounter = 0;

  private static Holding newHolding() {
    holdingCounter++;
    return new Holding().setType(CASH).setValue(BigDecimal.valueOf(holdingCounter));
  }

  private CacheStorageAbstract createMockWithConstructor(SecurityDataPort securityDataPort, CacheEntityMapper mapper,
      CoreRedisCacheRepository cacheRepo) {
    return mock(CacheStorageAbstract.class,
        withSettings().useConstructor(securityDataPort, mapper, cacheRepo, null));
  }

  // ---- synchronizeAndLoad tests ----

  @Test
  void synchronizeAndLoad_verifyLoadCachesForHoldings() {
    // SETUP
    final CoreRedisCacheRepository cacheRepo = mock(CoreRedisCacheRepository.class);
    final CacheStorageAbstract m = createMockWithConstructor(null, null, cacheRepo);

    final List<Holding> holdings = List.of(newHolding());
    final List<DataProvider> providers = List.of(EAGLE);

    doCallRealMethod().when(m).synchronizeAndLoad(any(), any(), any());
    // ACT
    m.synchronizeAndLoad(holdings, providers, CacheCategory.STOCKS);

    // VERIFY
    verify(m).loadCachesForHoldings(holdings, providers);
  }

  @Test
  void synchronizeAndLoad_verifyFilterCachedResponses() {
    // SETUP
    final CoreRedisCacheRepository cacheRepo = mock(CoreRedisCacheRepository.class);
    final CacheStorageAbstract m = createMockWithConstructor(null, null, cacheRepo);

    final List<Holding> holdings = List.of(newHolding());
    final HashMap map = new HashMap();
    when(m.loadCachesForHoldings(any(), any())).thenReturn(map);

    doCallRealMethod().when(m).synchronizeAndLoad(any(), any(), any());
    // ACT
    final List<DataProvider> providers = List.of(EAGLE);
    m.synchronizeAndLoad(holdings, providers, CacheCategory.STOCKS);

    // VERIFY
    verify(m).filterCachedResponses(argThat(arg -> arg == map));
  }

  @Test
  void synchronizeAndLoad_verifyFetchUncachedHoldings() {
    // SETUP
    final CoreRedisCacheRepository cacheRepo = mock(CoreRedisCacheRepository.class);
    final CacheStorageAbstract m = createMockWithConstructor(null, null, cacheRepo);

    final List<Holding> holdings = List.of(newHolding());

    final Holding h = newHolding();
    when(m.filterCachedResponses(any())).thenReturn(Map.of(h, mock(RedisId.class)));
    when(m.convertToDomainMap(any())).thenReturn(Map.of(h, new Object()));

    doCallRealMethod().when(m).synchronizeAndLoad(any(), any(), any());
    // ACT
    final List<DataProvider> providers = List.of(EAGLE);
    m.synchronizeAndLoad(holdings, providers, CacheCategory.STOCKS);

    // VERIFY
    verify(m).fetchUncachedHoldings(argThat(arg -> arg == holdings), eq(providers), eq(List.of(h)));
  }

  @Test
  void synchronizeAndLoad_checkResults() {
    // SETUP
    final CoreRedisCacheRepository cacheRepo = mock(CoreRedisCacheRepository.class);
    final CacheStorageAbstract m = createMockWithConstructor(null, null, cacheRepo);

    final HashMap cachedDomainModels = new HashMap();
    cachedDomainModels.put("1", "1");
    final HashMap fetchedDomainModels = new HashMap();
    fetchedDomainModels.put("12", "12");

    when(m.filterCachedResponses(any())).thenReturn(new HashMap<>());
    when(m.convertToDomainMap(any())).thenReturn(cachedDomainModels);
    when(m.fetchUncachedHoldings(any(), any(), any())).thenReturn(fetchedDomainModels);

    doCallRealMethod().when(m).synchronizeAndLoad(any(), any(), any());
    // ACT
    final Map actual = m.synchronizeAndLoad(List.of(newHolding()), List.of(), CacheCategory.STOCKS);

    // VERIFY
    assertEquals(Map.of("1", "1", "12", "12"), actual);
  }

  @Test
  void synchronizeAndLoad_checkResult_whenHoldingsIsEmpty() {
    // SETUP
    final var sut = mock(CacheStorageAbstract.class);
    final var holdings = mock(List.class);

    when(holdings.isEmpty()).thenReturn(true);
    doCallRealMethod().when(sut).synchronizeAndLoad(any(), any(), any());

    // ACT
    final Map actual = sut.synchronizeAndLoad(holdings, mock(List.class), CacheCategory.ETF);

    // VERIFY
    assertTrue(actual.isEmpty());
    verify(sut).synchronizeAndLoad(any(), any(), any());
    verifyNoMoreInteractions(sut);
  }

  // ---- saveToCache tests ----

  @Test
  void saveToCache_verifySave() {
    // SETUP
    final CoreRedisCacheRepository cacheRepo = mock(CoreRedisCacheRepository.class);
    final CacheStorageAbstract m = createMockWithConstructor(null, null, cacheRepo);

    final DataProvider eagle = EAGLE;
    final String providersId = buildIdBasedOnProviders(List.of(eagle));

    final RedisId res = mock(RedisId.class);
    when(res.getProvider()).thenReturn(eagle.name());
    when(res.getProviders()).thenReturn(providersId);
    when(res.getHoldingId()).thenReturn("ID");
    final Map<Object, Object> map = Map.of(newHolding(), res);

    doCallRealMethod().when(m).saveToCache(anyMap(), any());
    // ACT
    m.saveToCache(map, List.of(eagle));

    // VERIFY
    verify(cacheRepo).save(res);
  }

  @Test
  void saveToCache_verifySaveWhenProvidersAreAllNull() {
    // SETUP
    final CoreRedisCacheRepository cacheRepo = mock(CoreRedisCacheRepository.class);
    final CacheStorageAbstract m = createMockWithConstructor(null, null, cacheRepo);

    final RedisId res = mock(RedisId.class);
    when(res.getProvider()).thenReturn("");
    when(res.getProviders()).thenReturn("");
    when(res.getHoldingId()).thenReturn("ID");

    final Map<Object, Object> map = Map.of(newHolding(), res);

    doCallRealMethod().when(m).saveToCache(anyMap(), any());
    // ACT
    m.saveToCache(map, null);

    // VERIFY
    verify(cacheRepo).save(res);
  }

  @Test
  void saveToCache_verifySaveWhenProvidersAreEmpty() {
    // SETUP
    final CoreRedisCacheRepository cacheRepo = mock(CoreRedisCacheRepository.class);
    final CacheStorageAbstract m = createMockWithConstructor(null, null, cacheRepo);

    final RedisId res = mock(RedisId.class);
    when(res.getProvider()).thenReturn("");
    when(res.getProviders()).thenReturn("");
    when(res.getHoldingId()).thenReturn("ID");

    final Map<Object, Object> map = Map.of(newHolding(), res);

    doCallRealMethod().when(m).saveToCache(anyMap(), any());
    // ACT
    m.saveToCache(map, List.of());

    // VERIFY
    verify(cacheRepo).save(res);
  }

  // ---- filterCachedResponses tests ----

  @Test
  void filterCachedResponses_verify() {
    // SETUP
    final CacheStorageAbstract m = mock(CacheStorageAbstract.class);

    final Holding h = new Holding().setType(CASH);
    final Optional<RedisId> res = Optional.of(mock(RedisId.class));

    final Holding h2 = new Holding().setType(US_ETF).setValue(BigDecimal.valueOf(-1));
    final Map<Holding, Optional<RedisId>> of = Map.of(h, res, h2, Optional.empty());

    doCallRealMethod().when(m).filterCachedResponses(anyMap());
    // ACT
    final Map actual = m.filterCachedResponses(of);

    // VERIFY
    assertEquals(Map.of(h, res.orElseThrow()), actual);
  }

  // ---- filterHoldingsBy tests ----

  @Test
  void filterHoldingsBy_verify() {
    // SETUP
    final CacheStorageAbstract m = mock(CacheStorageAbstract.class);

    final Holding h = new Holding().setType(CASH);
    final List<Holding> holdings = List.of(h, new Holding().setType(US_ETF));

    doCallRealMethod().when(m).filterHoldingsBy(any(), any());
    // ACT
    final List set = m.filterHoldingsBy(holdings, o -> ((Holding) o).getType() == CASH);

    // VERIFY
    assertEquals(List.of(h), set);
  }

  // ---- queryCacheForEnteredDataProviders tests ----

  @Test
  void queryCacheForEnteredDataProviders_notExists() {
    // SETUP
    final CoreRedisCacheRepository cacheRepo = mock(CoreRedisCacheRepository.class);
    final CacheStorageAbstract m = createMockWithConstructor(null, null, cacheRepo);

    when(cacheRepo.findOneByHoldingIdAndProvider(any(), any())).thenReturn(Optional.empty());
    when(cacheRepo.findOneByHoldingIdAndProviders(any(), any())).thenReturn(Optional.empty());

    final Holding h = newHolding();

    final List<DataProvider> eagle = List.of(EAGLE);

    doCallRealMethod().when(m).queryCacheForEnteredDataProviders(any(), any());
    // ACT
    final Optional actual = m.queryCacheForEnteredDataProviders(eagle, h);

    // VERIFY
    verify(cacheRepo).findOneByHoldingIdAndProviders(h.generateUserIdentifier(), buildIdBasedOnProviders(eagle));
    assertEquals(Optional.empty(), actual);
  }

  @Test
  void queryCacheForEnteredDataProviders_exists() {
    // SETUP
    final CoreRedisCacheRepository cacheRepo = mock(CoreRedisCacheRepository.class);
    final CacheStorageAbstract m = createMockWithConstructor(null, null, cacheRepo);

    final RedisId res = mock(RedisId.class);
    when(cacheRepo.findOneByHoldingIdAndProviders(any(), any())).thenReturn(Optional.of(res));

    final Holding h = newHolding();

    final List<DataProvider> eagle = List.of(EAGLE);

    doCallRealMethod().when(m).queryCacheForEnteredDataProviders(any(), any());
    // ACT
    final Optional actual = m.queryCacheForEnteredDataProviders(eagle, h);

    // VERIFY
    verify(cacheRepo).findOneByHoldingIdAndProviders(h.generateUserIdentifier(), buildIdBasedOnProviders(eagle));
    verify(cacheRepo, times(0)).findOneByHoldingIdAndProvider(h.generateUserIdentifier(), eagle.get(0).name());
    assertEquals(Optional.of(res), actual);
  }

  @Test
  void queryCacheForEnteredDataProviders_fallbackToSingleProvider() {
    // SETUP
    final CoreRedisCacheRepository cacheRepo = mock(CoreRedisCacheRepository.class);
    final CacheStorageAbstract m = createMockWithConstructor(null, null, cacheRepo);

    final RedisId res = mock(RedisId.class);

    when(cacheRepo.findOneByHoldingIdAndProviders(any(), any())).thenReturn(Optional.empty());
    when(cacheRepo.findOneByHoldingIdAndProvider(any(), any())).thenReturn(Optional.of(res));

    final Holding h = newHolding();

    final List<DataProvider> providers = List.of(EAGLE, MORNINGSTAR);

    doCallRealMethod().when(m).queryCacheForEnteredDataProviders(any(), any());
    // ACT
    final Optional actual = m.queryCacheForEnteredDataProviders(providers, h);

    // VERIFY
    verify(cacheRepo).findOneByHoldingIdAndProvider(h.generateUserIdentifier(), providers.get(0).name());
    verify(cacheRepo, times(0)).findOneByHoldingIdAndProvider(h.generateUserIdentifier(), providers.get(1).name());
    assertEquals(Optional.of(res), actual);
  }

  // ---- pickUpProviderBasedOnPriority tests ----

  @Test
  void pickUpProviderBasedOnPriority_checkResult() {
    // SETUP
    final CacheStorageAbstract m = mock(CacheStorageAbstract.class);

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
    final CacheStorageAbstract m = mock(CacheStorageAbstract.class);

    final RedisId res2 = mock(RedisId.class);
    when(res2.getProvider()).thenReturn("");

    doCallRealMethod().when(m).pickUpProviderBasedOnPriority(any());
    // ACT
    final Optional actual = m.pickUpProviderBasedOnPriority(List.of(res2));

    // VERIFY
    assertEquals(Optional.of(res2), actual);
  }

  // ---- queryCacheForHolding tests ----

  @Test
  void queryCacheForHolding_verifyQueryCacheForEnteredDataProviders() {
    // SETUP
    final CoreRedisCacheRepository cacheRepo = mock(CoreRedisCacheRepository.class);
    final CacheStorageAbstract m = createMockWithConstructor(null, null, cacheRepo);

    final List<DataProvider> eagle = List.of(EAGLE);
    final Holding h = newHolding();

    doCallRealMethod().when(m).queryCacheForHolding(any(), any());
    // ACT
    m.queryCacheForHolding(h, eagle);

    // VERIFY
    verify(m).queryCacheForEnteredDataProviders(eagle, h);
  }

  @Test
  void queryCacheForHolding_verifyFindAllByHoldingIdEmpty() {
    // SETUP
    final CoreRedisCacheRepository cacheRepo = mock(CoreRedisCacheRepository.class);
    final CacheStorageAbstract m = createMockWithConstructor(null, null, cacheRepo);

    final List<DataProvider> eagle = List.of();
    final Holding h = newHolding();

    when(cacheRepo.findAllByHoldingId(any())).thenReturn(List.of());

    doCallRealMethod().when(m).queryCacheForHolding(any(), any());
    // ACT
    final Optional actual = m.queryCacheForHolding(h, eagle);

    // VERIFY
    verify(cacheRepo).findAllByHoldingId(h.generateUserIdentifier());
    assertEquals(Optional.empty(), actual);
  }

  @Test
  void queryCacheForHolding_verifyPickUpProviderBasedOnPriority() {
    // SETUP
    final CoreRedisCacheRepository cacheRepo = mock(CoreRedisCacheRepository.class);
    final CacheStorageAbstract m = createMockWithConstructor(null, null, cacheRepo);

    final List<DataProvider> eagle = List.of();
    final Holding h = newHolding();

    final List<RedisId> all = List.of(mock(RedisId.class));
    when(cacheRepo.findAllByHoldingId(any())).thenReturn(all);

    doCallRealMethod().when(m).queryCacheForHolding(any(), any());
    // ACT
    m.queryCacheForHolding(h, eagle);

    // VERIFY
    verify(m).pickUpProviderBasedOnPriority(all);
  }

  @Test
  void queryCacheForHolding_checkResult() {
    // SETUP
    final CoreRedisCacheRepository cacheRepo = mock(CoreRedisCacheRepository.class);
    final CacheStorageAbstract m = createMockWithConstructor(null, null, cacheRepo);

    final List<DataProvider> eagle = List.of();
    final Holding h = newHolding();

    final List<RedisId> all = List.of(mock(RedisId.class));
    when(cacheRepo.findAllByHoldingId(any())).thenReturn(all);

    final Optional<Object> expected = Optional.of(mock(Object.class));
    when(m.pickUpProviderBasedOnPriority(any())).thenReturn(expected);

    doCallRealMethod().when(m).queryCacheForHolding(any(), any());
    // ACT
    final Optional actual = m.queryCacheForHolding(h, eagle);

    // VERIFY
    assertSame(expected, actual);
  }

  // ---- loadCachesForHoldings tests ----

  @Test
  void loadCachesForHoldings_checkResult() {
    // SETUP
    final CoreRedisCacheRepository cacheRepo = mock(CoreRedisCacheRepository.class);
    final CacheStorageAbstract m = createMockWithConstructor(null, null, cacheRepo);

    final List<DataProvider> eagle = List.of(EAGLE);
    final Holding h = newHolding();

    when(m.queryCacheForHolding(any(), any())).thenReturn(Optional.empty());

    doCallRealMethod().when(m).loadCachesForHoldings(any(), any());
    // ACT
    final Map map = m.loadCachesForHoldings(List.of(h), eagle);

    // VERIFY
    assertEquals(Map.of(h, Optional.empty()), map);
  }

  // ---- convenience method tests ----

  @Test
  void loadBenchOfFundCanada_delegatesToSynchronizeAndLoad() {
    // SETUP
    final CacheStorageAbstract m = mock(CacheStorageAbstract.class);

    final List<Holding> holdings = List.of(newHolding());
    final List<DataProvider> providers = List.of(EAGLE);

    final HashMap expected = new HashMap();
    when(m.synchronizeAndLoad(any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(m).loadBenchOfFundCanada(any(), any());
    // ACT
    final Map actual = m.loadBenchOfFundCanada(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
    verify(m).synchronizeAndLoad(eq(holdings), eq(providers), eq(CacheCategory.CANADA_MUTUAL_FUNDS));
  }

  @Test
  void loadBenchOfFixedIncomes_delegatesToSynchronizeAndLoad() {
    // SETUP
    final CacheStorageAbstract m = mock(CacheStorageAbstract.class);

    final List<Holding> holdings = List.of(newHolding());
    final List<DataProvider> providers = List.of(EAGLE);

    final HashMap expected = new HashMap();
    when(m.synchronizeAndLoad(any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(m).loadBenchOfFixedIncomes(any(), any());
    // ACT
    final Map actual = m.loadBenchOfFixedIncomes(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
    verify(m).synchronizeAndLoad(eq(holdings), eq(providers), eq(CacheCategory.FIXED_INCOME));
  }

  @Test
  void loadBenchOfSeparatelyManagedAccounts_delegatesToSynchronizeAndLoad() {
    // SETUP
    final CacheStorageAbstract m = mock(CacheStorageAbstract.class);

    final List<Holding> holdings = List.of(newHolding());
    final List<DataProvider> providers = List.of(EAGLE);

    final HashMap expected = new HashMap();
    when(m.synchronizeAndLoad(any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(m).loadBenchOfSeparatelyManagedAccounts(any(), any());
    // ACT
    final Map actual = m.loadBenchOfSeparatelyManagedAccounts(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
    verify(m).synchronizeAndLoad(eq(holdings), eq(providers), eq(CacheCategory.SEPARATELY_MANAGED_ACCOUNT));
  }

  @Test
  void loadForBenchOfStock_delegatesToSynchronizeAndLoad() {
    // SETUP
    final CacheStorageAbstract m = mock(CacheStorageAbstract.class);

    final List<Holding> holdings = List.of(newHolding());
    final List<DataProvider> providers = List.of(EAGLE);

    final HashMap expected = new HashMap();
    when(m.synchronizeAndLoad(any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(m).loadForBenchOfStock(any(), any());
    // ACT
    final Map actual = m.loadForBenchOfStock(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
    verify(m).synchronizeAndLoad(eq(holdings), eq(providers), eq(CacheCategory.STOCKS));
  }

  @Test
  void loadForBenchOfEtfCanada_delegatesToSynchronizeAndLoad() {
    // SETUP
    final CacheStorageAbstract m = mock(CacheStorageAbstract.class);

    final List<Holding> holdings = List.of(newHolding());
    final List<DataProvider> providers = List.of(EAGLE);

    final HashMap expected = new HashMap();
    when(m.synchronizeAndLoad(any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(m).loadForBenchOfEtfCanada(any(), any());
    // ACT
    final Map actual = m.loadForBenchOfEtfCanada(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
    verify(m).synchronizeAndLoad(eq(holdings), eq(providers), eq(CacheCategory.CANADA_ETF));
  }

  @Test
  void loadForBenchOfEtfUs_delegatesToSynchronizeAndLoad() {
    // SETUP
    final CacheStorageAbstract m = mock(CacheStorageAbstract.class);

    final List<Holding> holdings = List.of(newHolding());
    final List<DataProvider> providers = List.of(EAGLE);

    final HashMap expected = new HashMap();
    when(m.synchronizeAndLoad(any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(m).loadForBenchOfEtfUs(any(), any());
    // ACT
    final Map actual = m.loadForBenchOfEtfUs(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
    verify(m).synchronizeAndLoad(eq(holdings), eq(providers), eq(CacheCategory.US_ETF));
  }

  @Test
  void loadForBenchOfBenchmarks_delegatesToSynchronizeAndLoad() {
    // SETUP
    final CacheStorageAbstract m = mock(CacheStorageAbstract.class);

    final List<Holding> holdings = List.of(newHolding());
    final List<DataProvider> providers = List.of(EAGLE);

    final HashMap expected = new HashMap();
    when(m.synchronizeAndLoad(any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(m).loadForBenchOfBenchmarks(any(), any());
    // ACT
    final Map actual = m.loadForBenchOfBenchmarks(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
    verify(m).synchronizeAndLoad(eq(holdings), eq(providers), eq(CacheCategory.BENCHMARK_INDEXES));
  }

}
