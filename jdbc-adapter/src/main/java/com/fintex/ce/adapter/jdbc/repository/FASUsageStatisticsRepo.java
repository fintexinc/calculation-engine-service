package com.fintex.ce.adapter.jdbc.repository;

import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.constant.CacheNameEntity;
import com.fintex.ce.adapter.jdbc.entity.SMUsageStatistics;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FASUsageStatisticsRepo extends CrudRepository<SMUsageStatistics, Long> {

  boolean existsByCacheNameEntityAndCacheCategoryAndHoldingIdAndProvider(
      CacheNameEntity cacheNameEntity, CacheCategory cacheCategory, String holdingId, String dataProvider);

  @Modifying
  @Transactional
  @Query("UPDATE SMUsageStatistics SET day0Count = day0Count + 1 WHERE cacheNameEntity = ?1 and cacheCategory = ?2 and holdingId = ?3 and provider = ?4")
  int incrementSunday(CacheNameEntity cacheNameEntity, CacheCategory cacheCategory, String holdingId,
      String dataProvider);

  @Modifying
  @Transactional
  @Query("UPDATE SMUsageStatistics SET day1Count = day1Count + 1 WHERE cacheNameEntity = ?1 and cacheCategory = ?2 and holdingId = ?3 and provider = ?4")
  int incrementMonday(CacheNameEntity cacheNameEntity, CacheCategory cacheCategory, String holdingId,
      String dataProvider);

  @Modifying
  @Transactional
  @Query("UPDATE SMUsageStatistics SET day2Count = day2Count + 1 WHERE cacheNameEntity = ?1 and cacheCategory = ?2 and holdingId = ?3 and provider = ?4")
  int incrementTuesday(CacheNameEntity cacheNameEntity, CacheCategory cacheCategory, String holdingId,
      String dataProvider);

  @Modifying
  @Transactional
  @Query("UPDATE SMUsageStatistics SET day3Count = day3Count + 1 WHERE cacheNameEntity = ?1 and cacheCategory = ?2 and holdingId = ?3 and provider = ?4")
  int incrementWednesday(CacheNameEntity cacheNameEntity, CacheCategory cacheCategory, String holdingId,
      String dataProvider);

  @Modifying
  @Transactional
  @Query("UPDATE SMUsageStatistics SET day4Count = day4Count + 1 WHERE cacheNameEntity = ?1 and cacheCategory = ?2 and holdingId = ?3 and provider = ?4")
  int incrementThursday(CacheNameEntity cacheNameEntity, CacheCategory cacheCategory, String holdingId,
      String dataProvider);

  @Modifying
  @Transactional
  @Query("UPDATE SMUsageStatistics SET day5Count = day5Count + 1 WHERE cacheNameEntity = ?1 and cacheCategory = ?2 and holdingId = ?3 and provider = ?4")
  int incrementFriday(CacheNameEntity cacheNameEntity, CacheCategory cacheCategory, String holdingId,
      String dataProvider);

  @Modifying
  @Transactional
  @Query("UPDATE SMUsageStatistics SET day6Count = day6Count + 1 WHERE cacheNameEntity = ?1 and cacheCategory = ?2 and holdingId = ?3 and provider = ?4")
  int incrementSaturday(CacheNameEntity cacheNameEntity, CacheCategory cacheCategory, String holdingId,
      String dataProvider);

  @Query(value = "SELECT 'x'", nativeQuery = true)
  String isDbHealthy();

  @Modifying
  @Transactional
  @Query("UPDATE SMUsageStatistics SET" +
      " day0Count = CASE WHEN 0 = :dayOfWeek THEN 0 ELSE day0Count END, " +
      " day1Count = CASE WHEN 1 = :dayOfWeek THEN 0 ELSE day1Count END, " +
      " day2Count = CASE WHEN 2 = :dayOfWeek THEN 0 ELSE day2Count END, " +
      " day3Count = CASE WHEN 3 = :dayOfWeek THEN 0 ELSE day3Count END, " +
      " day4Count = CASE WHEN 4 = :dayOfWeek THEN 0 ELSE day4Count END, " +
      " day5Count = CASE WHEN 5 = :dayOfWeek THEN 0 ELSE day5Count END, " +
      " day6Count = CASE WHEN 6 = :dayOfWeek THEN 0 ELSE day6Count END")
  int updateDayCountToZeroForDayOfWeek(final Integer dayOfWeek);

}
