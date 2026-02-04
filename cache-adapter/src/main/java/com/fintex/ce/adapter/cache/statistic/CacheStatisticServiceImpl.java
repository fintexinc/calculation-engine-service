package com.fintex.ce.adapter.cache.statistic;

import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.constant.CacheNameEntity;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.adapter.jdbc.entity.SMUsageStatistics;
import com.fintex.ce.adapter.cache.entity.core.RedisId;
import com.fintex.ce.adapter.jdbc.repository.FASUsageStatisticsRepo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.time.DayOfWeek.*;

@Service
@Log4j2
public class CacheStatisticServiceImpl implements CacheStatisticService {
  private final FASUsageStatisticsRepo statisticsRepo;
  private final Object lock = new Object();

  public Map<DayOfWeek, Function<Parameters, Integer>> dayOfWeekMap;

  @Autowired
  public CacheStatisticServiceImpl(FASUsageStatisticsRepo statisticsRepo) {
    this.statisticsRepo = statisticsRepo;
    this.dayOfWeekMap = initDayOfWeekFunctionMap();
  }

  @Override
  public <H extends Holding, R extends RedisId> void analyse(final Map<H, R> responses,
      final CacheNameEntity cacheNameEntity,
      final CacheCategory cacheCategory) {
    try {
      performAnalysis(responses, cacheNameEntity, cacheCategory);
    } catch (Exception e) {
      log.error("While performing cache analysis", e);
    }
  }

  public <H extends Holding, R extends RedisId> void performAnalysis(final Map<H, R> responses,
      final CacheNameEntity cacheNameEntity,
      final CacheCategory cacheCategory) {
    createNewRecords(responses, cacheNameEntity, cacheCategory);
    incrementForCurrentDay(responses, cacheNameEntity, cacheCategory);
  }

  /**
   * Increments usages (+1) for current day for entered holdings
   *
   * @param responses
   *          responses from FDS
   * @param cacheNameEntity
   *          cache name prefix
   * @param cacheCategory
   *          cache category
   * @param <H>
   *          holding type
   * @param <R>
   *          response type
   */
  public <H extends Holding, R extends RedisId> void incrementForCurrentDay(final Map<H, R> responses,
      final CacheNameEntity cacheNameEntity,
      final CacheCategory cacheCategory) {
    responses.forEach((h, r) -> {
      final DayOfWeek dayOfWeek = currentDayOfWeek();
      final Parameters parameters = new Parameters(cacheNameEntity, cacheCategory, h.generateUserIdentifier(), r
          .getProvider());
      dayOfWeekMap.get(dayOfWeek).apply(parameters);
    });
  }

  /**
   * Creates new records in the DB for those holdings which do not have statistic records yet
   *
   * @param responses
   *          responses from FDS
   * @param cacheNameEntity
   *          cache name prefix
   * @param cacheCategory
   *          cache category
   * @param <H>
   *          holding type
   * @param <R>
   *          response type
   */
  public <H extends Holding, R extends RedisId> void createNewRecords(final Map<H, R> responses,
      final CacheNameEntity cacheNameEntity,
      final CacheCategory cacheCategory) {
    final List<SMUsageStatistics> allToBeSaved;
    // to avoid DB deadlocks and synchronise access to DB
    synchronized (lock) {
      allToBeSaved = mappedUnsavedHoldings(responses, cacheNameEntity, cacheCategory);
    }
    if (!allToBeSaved.isEmpty()) {
      final Object[] ids = allToBeSaved.stream().map(SMUsageStatistics::getHoldingId).toArray();
      log.info("Going to create new cache statistic records for {}", Arrays.toString(ids));
      statisticsRepo.saveAll(allToBeSaved);
    }
  }

  /**
   * Filters and maps holdings which do not have their corresponding records in the DB
   *
   * @param responses
   *          responses from FDS
   * @param cacheNameEntity
   *          cache name prefix
   * @param cacheCategory
   *          cache category
   * @param <H>
   *          holding type
   * @param <R>
   *          response type
   * @return mapped statistic entities
   */
  public <H extends Holding, R extends RedisId> List<SMUsageStatistics> mappedUnsavedHoldings(final Map<H, R> responses,
      final CacheNameEntity cacheNameEntity,
      final CacheCategory cacheCategory) {
    return responses.entrySet().stream()
        .filter(
            e -> !statisticsRepo.existsByCacheNameEntityAndCacheCategoryAndHoldingIdAndProvider(
                cacheNameEntity, cacheCategory, e.getKey().generateUserIdentifier(), e.getValue().getProvider()))
        .map(e -> mapToStatisticEntity(e.getKey(), e.getValue(), cacheNameEntity, cacheCategory)).toList();
  }

  public SMUsageStatistics mapToStatisticEntity(final Holding h, final RedisId r, final CacheNameEntity cacheNameEntity,
      final CacheCategory cacheCategory) {
    final SMUsageStatistics entity = new SMUsageStatistics();
    return entity
        .setCacheNameEntity(cacheNameEntity)
        .setCacheCategory(cacheCategory)
        .setHoldingType(h.getType())
        .setHoldingId(h.generateUserIdentifier())
        .setHoldingIdType(h.getHoldingIdentifier())
        .setProvider(r.getProvider());
  }

  public DayOfWeek currentDayOfWeek() {
    return LocalDate.now().getDayOfWeek();
  }

  public Map<DayOfWeek, Function<Parameters, Integer>> initDayOfWeekFunctionMap() {
    Map<DayOfWeek, Function<Parameters, Integer>> map = new EnumMap<>(DayOfWeek.class);
    map.put(SUNDAY, args -> statisticsRepo.incrementSunday(args.cacheNameEntity, args.cacheCategory, args.holdingId,
        args.dataProvider));
    map.put(MONDAY, args -> statisticsRepo.incrementMonday(args.cacheNameEntity, args.cacheCategory, args.holdingId,
        args.dataProvider));
    map.put(TUESDAY, args -> statisticsRepo.incrementTuesday(args.cacheNameEntity, args.cacheCategory, args.holdingId,
        args.dataProvider));
    map.put(WEDNESDAY, args -> statisticsRepo.incrementWednesday(args.cacheNameEntity, args.cacheCategory,
        args.holdingId, args.dataProvider));
    map.put(THURSDAY, args -> statisticsRepo.incrementThursday(args.cacheNameEntity, args.cacheCategory, args.holdingId,
        args.dataProvider));
    map.put(FRIDAY, args -> statisticsRepo.incrementFriday(args.cacheNameEntity, args.cacheCategory, args.holdingId,
        args.dataProvider));
    map.put(SATURDAY, args -> statisticsRepo.incrementSaturday(args.cacheNameEntity, args.cacheCategory, args.holdingId,
        args.dataProvider));
    return map;
  }

  @Data
  @AllArgsConstructor
  public static class Parameters {
    private final CacheNameEntity cacheNameEntity;
    private final CacheCategory cacheCategory;
    private final String holdingId;
    private final String dataProvider;
  }

}
