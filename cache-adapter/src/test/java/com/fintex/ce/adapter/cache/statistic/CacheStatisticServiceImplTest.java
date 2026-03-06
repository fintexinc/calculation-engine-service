package com.fintex.ce.adapter.cache.statistic;

import com.fintex.ce.adapter.cache.entity.core.RedisId;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticServiceImpl;
import com.fintex.ce.adapter.jdbc.entity.SMUsageStatistics;
import com.fintex.ce.adapter.jdbc.repository.FASUsageStatisticsRepo;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.constant.CacheNameEntity;
import com.fintex.ce.domain.enumeration.HoldingIdentifierType;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.time.DayOfWeek.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CacheStatisticServiceImplTest {

  @Test
  void initDayOfWeekFunctionMap_checkResult() {
    // SETUP
    FASUsageStatisticsRepo repo = mock(FASUsageStatisticsRepo.class);

    final CacheStatisticServiceImpl c = mock(CacheStatisticServiceImpl.class,
        withSettings().useConstructor(repo));

    doCallRealMethod().when(c).initDayOfWeekFunctionMap();
    // ACT
    final Map<DayOfWeek, Function<CacheStatisticServiceImpl.Parameters, Integer>> map = c.initDayOfWeekFunctionMap();

    // VERIFY
    map.get(SUNDAY).apply(new CacheStatisticServiceImpl.Parameters(CacheNameEntity.MER, CacheCategory.STOCKS, "2",
        "3"));
    verify(repo).incrementSunday(CacheNameEntity.MER, CacheCategory.STOCKS, "2", "3");

    map.get(MONDAY).apply(new CacheStatisticServiceImpl.Parameters(CacheNameEntity.MER, CacheCategory.STOCKS, "2",
        "3"));
    verify(repo).incrementMonday(CacheNameEntity.MER, CacheCategory.STOCKS, "2", "3");

    map.get(TUESDAY).apply(new CacheStatisticServiceImpl.Parameters(CacheNameEntity.MER, CacheCategory.STOCKS, "2",
        "3"));
    verify(repo).incrementTuesday(CacheNameEntity.MER, CacheCategory.STOCKS, "2", "3");

    map.get(WEDNESDAY).apply(new CacheStatisticServiceImpl.Parameters(CacheNameEntity.MER, CacheCategory.STOCKS, "2",
        "3"));
    verify(repo).incrementWednesday(CacheNameEntity.MER, CacheCategory.STOCKS, "2", "3");

    map.get(THURSDAY).apply(new CacheStatisticServiceImpl.Parameters(CacheNameEntity.MER, CacheCategory.STOCKS, "2",
        "3"));
    verify(repo).incrementThursday(CacheNameEntity.MER, CacheCategory.STOCKS, "2", "3");

    map.get(FRIDAY).apply(new CacheStatisticServiceImpl.Parameters(CacheNameEntity.MER, CacheCategory.STOCKS, "2",
        "3"));
    verify(repo).incrementFriday(CacheNameEntity.MER, CacheCategory.STOCKS, "2", "3");

    map.get(SATURDAY).apply(new CacheStatisticServiceImpl.Parameters(CacheNameEntity.MER, CacheCategory.STOCKS, "2",
        "3"));
    verify(repo).incrementSaturday(CacheNameEntity.MER, CacheCategory.STOCKS, "2", "3");
  }

  @Test
  void currentDayOfWeek_checkResult() {
    // SETUP
    final CacheStatisticServiceImpl c = mock(CacheStatisticServiceImpl.class);

    doCallRealMethod().when(c).currentDayOfWeek();
    // ACT
    final DayOfWeek dayOfWeek = c.currentDayOfWeek();

    // VERIFY
    assertEquals(LocalDate.now().getDayOfWeek(), dayOfWeek);
  }

  @Test
  void mapToStatisticEntity_checkResult() {
    // SETUP
    final CacheStatisticServiceImpl c = mock(CacheStatisticServiceImpl.class);

    final CacheNameEntity mer = CacheNameEntity.MER;
    final CacheCategory stocks = CacheCategory.STOCKS;

    final Holding h = new FundSeriesHolding();
    h.setType(HoldingType.US_STOCKS);
    h.setHoldingIdentifier(HoldingIdentifierType.FUNDSERV);
    ((FundSeriesHolding) h).setFundServCode("ID");
    final RedisId r = mock(RedisId.class);
    when(r.getProvider()).thenReturn("PROVIDER");

    doCallRealMethod().when(c).mapToStatisticEntity(any(), any(), any(), any());
    // ACT
    final SMUsageStatistics actual = c.mapToStatisticEntity(h, r, mer, stocks);

    // VERIFY
    final SMUsageStatistics expected = new SMUsageStatistics()
        .setCacheNameEntity(mer)
        .setCacheCategory(stocks)
        .setProvider("PROVIDER")
        .setHoldingType(HoldingType.US_STOCKS)
        .setHoldingId("ID")
        .setHoldingIdType(HoldingIdentifierType.FUNDSERV);

    assertEquals(expected, actual);
  }

  @Test
  void mappedUnsavedHoldings_verifyExistsByCacheNameAndHoldingIdAndProvider() {
    // SETUP
    FASUsageStatisticsRepo repo = mock(FASUsageStatisticsRepo.class);

    final CacheStatisticServiceImpl c = mock(CacheStatisticServiceImpl.class,
        withSettings().useConstructor(repo));

    final FundSeriesHolding h = new FundSeriesHolding();
    h.setFundServCode("ID");
    h.setHoldingIdentifier(HoldingIdentifierType.FUNDSERV);
    final RedisId r = mock(RedisId.class);
    when(r.getProvider()).thenReturn("PROVIDER");

    final Map<Holding, RedisId> map = Map.of(h, r);

    doCallRealMethod().when(c).mappedUnsavedHoldings(any(), any(), any());
    // ACT
    c.mappedUnsavedHoldings(map, CacheNameEntity.MER, CacheCategory.US_ETF);

    // VERIFY
    verify(repo).existsByCacheNameEntityAndCacheCategoryAndHoldingIdAndProvider(CacheNameEntity.MER,
        CacheCategory.US_ETF, "ID", "PROVIDER");
  }

  @Test
  void mappedUnsavedHoldings_verifyMapToStatisticEntity() {
    // SETUP
    FASUsageStatisticsRepo repo = mock(FASUsageStatisticsRepo.class);

    final CacheStatisticServiceImpl c = mock(CacheStatisticServiceImpl.class,
        withSettings().useConstructor(repo));

    final FundSeriesHolding h = new FundSeriesHolding();
    h.setFundServCode("ID");
    h.setHoldingIdentifier(HoldingIdentifierType.FUNDSERV);
    final RedisId r = mock(RedisId.class);
    when(r.getProvider()).thenReturn("PROVIDER");

    final Map<Holding, RedisId> map = Map.of(h, r);

    when(repo.existsByCacheNameEntityAndCacheCategoryAndHoldingIdAndProvider(any(), any(), any(), any())).thenReturn(
        false);

    doCallRealMethod().when(c).mappedUnsavedHoldings(any(), any(), any());
    // ACT
    c.mappedUnsavedHoldings(map, CacheNameEntity.MER, CacheCategory.US_ETF);

    // VERIFY
    verify(c).mapToStatisticEntity(h, r, CacheNameEntity.MER, CacheCategory.US_ETF);
  }

  @Test
  void mappedUnsavedHoldings_checkResult() {
    // SETUP
    FASUsageStatisticsRepo repo = mock(FASUsageStatisticsRepo.class);

    final CacheStatisticServiceImpl c = mock(CacheStatisticServiceImpl.class,
        withSettings().useConstructor(repo));

    final FundSeriesHolding h = new FundSeriesHolding();
    h.setFundServCode("ID");
    h.setHoldingIdentifier(HoldingIdentifierType.FUNDSERV);
    final RedisId r = mock(RedisId.class);
    when(r.getProvider()).thenReturn("PROVIDER");

    final Map<Holding, RedisId> map = Map.of(h, r);

    when(repo.existsByCacheNameEntityAndCacheCategoryAndHoldingIdAndProvider(any(), any(), any(), any())).thenReturn(
        false);
    final SMUsageStatistics expected = mock(SMUsageStatistics.class);
    when(c.mapToStatisticEntity(any(), any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(c).mappedUnsavedHoldings(any(), any(), any());
    // ACT
    final List<SMUsageStatistics> actual = c.mappedUnsavedHoldings(map, CacheNameEntity.MER, CacheCategory.US_ETF);

    // VERIFY
    assertEquals(List.of(expected), actual);
  }

  @Test
  void createNewRecords_verifyMappedUnsavedHoldings() {
    // SETUP
    final CacheStatisticServiceImpl c = mock(CacheStatisticServiceImpl.class);

    when(c.mappedUnsavedHoldings(any(), any(), any())).thenReturn(List.of());

    final HashMap<Holding, RedisId> map = new HashMap<>();

    doCallRealMethod().when(c).mappedUnsavedHoldings(any(), any(), any());
    // ACT
    c.mappedUnsavedHoldings(map, CacheNameEntity.MER, CacheCategory.US_ETF);

    // VERIFY
    verify(c).mappedUnsavedHoldings(argThat(arg -> arg == map), eq(CacheNameEntity.MER), eq(CacheCategory.US_ETF));
  }

  @Test
  void createNewRecords_verifySaveAll() {
    // SETUP
    FASUsageStatisticsRepo repo = mock(FASUsageStatisticsRepo.class);

    final CacheStatisticServiceImpl c = mock(CacheStatisticServiceImpl.class,
        withSettings().useConstructor(repo));

    final SMUsageStatistics sm = mock(SMUsageStatistics.class);
    when(sm.getHoldingId()).thenReturn("");

    final List<SMUsageStatistics> list = List.of(sm);
    when(c.mappedUnsavedHoldings(any(), any(), any())).thenReturn(list);

    final HashMap<Holding, RedisId> map = new HashMap<>();

    doCallRealMethod().when(c).createNewRecords(any(), any(), any());
    // ACT
    c.createNewRecords(map, CacheNameEntity.MER, CacheCategory.US_ETF);

    // VERIFY
    verify(repo).saveAll(list);
  }

  @Test
  void incrementForCurrentDay_checkResult() {
    // SETUP
    final CacheStatisticServiceImpl c = mock(CacheStatisticServiceImpl.class);

    when(c.currentDayOfWeek()).thenReturn(MONDAY);

    c.dayOfWeekMap = mock(HashMap.class);
    final Function func = mock(Function.class);
    when(c.dayOfWeekMap.get(any())).thenReturn(func);

    final FundSeriesHolding h = new FundSeriesHolding();
    h.setFundServCode("ID");
    h.setHoldingIdentifier(HoldingIdentifierType.FUNDSERV);
    final RedisId r = mock(RedisId.class);
    when(r.getProvider()).thenReturn("PROVIDER");

    final Map<Holding, RedisId> map = Map.of(h, r);

    doCallRealMethod().when(c).incrementForCurrentDay(any(), any(), any());
    // ACT
    c.incrementForCurrentDay(map, CacheNameEntity.MER, CacheCategory.US_ETF);

    // VERIFY
    verify(c).currentDayOfWeek();
    verify(c.dayOfWeekMap).get(MONDAY);
    verify(func).apply(new CacheStatisticServiceImpl.Parameters(CacheNameEntity.MER, CacheCategory.US_ETF, "ID",
        "PROVIDER"));
  }

  @Test
  void performAnalysis_verifyCreateNewRecords() {
    // SETUP
    final CacheStatisticServiceImpl c = mock(CacheStatisticServiceImpl.class);

    final Map<Holding, RedisId> map = new HashMap<>();

    doCallRealMethod().when(c).performAnalysis(any(), any(), any());
    // ACT
    c.performAnalysis(map, CacheNameEntity.MER, CacheCategory.US_ETF);

    // VERIFY
    verify(c).createNewRecords(argThat(arg -> arg == map), eq(CacheNameEntity.MER), eq(CacheCategory.US_ETF));
  }

  @Test
  void performAnalysis_verifyIncrementForCurrentDay() {
    // SETUP
    final CacheStatisticServiceImpl c = mock(CacheStatisticServiceImpl.class);

    final Map<Holding, RedisId> map = new HashMap<>();

    doCallRealMethod().when(c).performAnalysis(any(), any(), any());
    // ACT
    c.performAnalysis(map, CacheNameEntity.MER, CacheCategory.US_ETF);

    // VERIFY
    verify(c).incrementForCurrentDay(argThat(arg -> arg == map), eq(CacheNameEntity.MER), eq(CacheCategory.US_ETF));
  }

  @Test
  void analyse_verifyPerformAnalysis() {
    // SETUP
    final CacheStatisticServiceImpl c = mock(CacheStatisticServiceImpl.class);

    final Map<Holding, RedisId> map = new HashMap<>();

    doCallRealMethod().when(c).analyse(any(), any(), any());
    // ACT
    c.analyse(map, CacheNameEntity.MER, CacheCategory.US_ETF);

    // VERIFY
    verify(c).performAnalysis(argThat(arg -> arg == map), eq(CacheNameEntity.MER), eq(CacheCategory.US_ETF));
  }

}