package com.fintex.ce.adapter.cache.statistic;

import com.google.common.collect.Lists;
import com.fintex.ce.constant.GeneralConstants;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingIdentifierType;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.constant.CacheNameEntity;
import com.fintex.ce.adapter.cache.config.properties.CacheWarmUpProperties;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.adapter.cache.dto.CacheRecordDTO;
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
import com.fintex.ce.domain.exception.GeneralRuntimeException;
import com.fintex.ce.adapter.jdbc.entity.SMUsageStatistics;
import com.fintex.ce.adapter.cache.entity.RCacheWarmUpDate;
import com.fintex.ce.adapter.jdbc.repository.FASUsageStatisticsRepo;
import com.fintex.ce.adapter.cache.repository.CacheWarmUpSchedulerDateRedisRepository;
import com.fintex.ce.adapter.cache.core.CacheStorageAbstract;
import com.fintex.ce.port.output.cache.CacheCleanupPort;
import com.fintex.ce.port.output.cache.CacheWarmUpPort;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fintex.ce.util.PortfolioUtils.calculateInitialPortfolioWeight;
import static java.math.BigDecimal.ONE;

@Service
@Slf4j
public class CacheWarmUpServiceImpl implements CacheWarmUpService {
  public static final int TEN = 10;
  private final FASUsageStatisticsRepo statisticsRepo;
  private final List<CacheStorageAbstract> cacheStorages;
  private final CacheWarmUpProperties cacheWarmUpProperties;
  private final CacheWarmUpSchedulerDateRedisRepository cacheWarmUpSchedulerDateRedisRepository;
  private final CacheCleanupPort cacheCleanupPort;

  public CacheWarmUpServiceImpl(FASUsageStatisticsRepo statisticsRepo,
      List<CacheStorageAbstract> cacheStorages,
      CacheWarmUpProperties cacheWarmUpProperties,
      CacheWarmUpSchedulerDateRedisRepository cacheWarmUpSchedulerDateRedisRepository,
      CacheCleanupPort cacheCleanupPort) {
    this.statisticsRepo = statisticsRepo;
    this.cacheStorages = cacheStorages;
    this.cacheWarmUpProperties = cacheWarmUpProperties;
    this.cacheWarmUpSchedulerDateRedisRepository = cacheWarmUpSchedulerDateRedisRepository;
    this.cacheCleanupPort = cacheCleanupPort;
  }

  @Override
  public void run() {
    cacheCleanupPort.clearCache();
    reloadCache();
    statisticsRepo.updateDayCountToZeroForDayOfWeek(LocalDate.now().getDayOfWeek().ordinal());
    cacheWarmUpSchedulerDateRedisRepository.save(new RCacheWarmUpDate().setZonedDateTime(ZonedDateTime.now()));
  }

  public void reloadCache() {
    log.info("Start reloading the cache");
    List<CacheRecordDTO> records = selectRecords();
    final Map<CacheNameEntity, List<CacheRecordDTO>> grouped = records.stream().collect(Collectors.groupingBy(
        CacheRecordDTO::getCacheNameEntity));

    grouped.forEach((cacheName, holdings) -> {
      log.info("cacheNameEntity: {} start loading {} holdings", cacheName, holdings.size());
      Lists.partition(holdings, TEN).forEach(partition -> {
        reloadCache(cacheName, partition);
      });
      log.info("cacheNameEntity: {} finish loading {} holdings", cacheName, holdings.size());
    });
    log.info("Finish reloading the cache");
  }

  public void reloadCache(final CacheNameEntity cacheNameEntity, final List<CacheRecordDTO> cacheRecords) {
    try {
      final CacheStorageAbstract storage = cacheStorages.stream().filter(s -> s.getCacheNameEntity().equals(
          cacheNameEntity)).findFirst().orElseThrow();
      final List<Holding> holdings = cacheRecords.stream().map(CacheRecordDTO::getHolding).distinct().collect(Collectors
          .toList());
      final List<DataProvider> providers = cacheRecords.stream().map(CacheRecordDTO::getProvider).filter(
          Objects::nonNull).distinct().collect(Collectors.toList());
      storage.load(holdings, new ArrayList<>(), providers, new ParamHolderDTO(calculateInitialPortfolioWeight(
          holdings)));
    } catch (final Exception e) {
      log.warn("Error when reloading cache : {}, exception: ", cacheNameEntity, e);
    }
  }

  /**
   * Selects records that have to be added into the cache again
   *
   * @return list of cached records
   */
  public List<CacheRecordDTO> selectRecords() {
    final Iterable<SMUsageStatistics> all = statisticsRepo.findAll();
    final List<CacheRecordDTO> records = new ArrayList<>();
    for (SMUsageStatistics statistics : all) {
      final CacheRecordDTO cacheRecordDTO = mapToCacheRecord(statistics);
      records.add(cacheRecordDTO);
    }
    return limitRecords(records);
  }

  public List<CacheRecordDTO> limitRecords(List<CacheRecordDTO> records) {
    final List<CacheRecordDTO> sorted = records.stream()
        .filter(r -> r.getNumberOfUsages() >= cacheWarmUpProperties.getMinNumberOfRecordUsages())
        .sorted((t1, t2) -> NumberUtils.compare(t2.getNumberOfUsages(), t1.getNumberOfUsages())).collect(Collectors
            .toList());
    if (sorted.size() <= cacheWarmUpProperties.getMinNumberOfRecords()) {
      return sorted;
    }
    int limit = getLimit(sorted);
    return sorted.stream().limit(limit).collect(Collectors.toList());
  }

  /**
   * Calculates the number of items that have to be loaded into cache: {min-number-of-records} +
   * (total-number-of-records * {percentage-factor}%)
   *
   * @param list
   *          cached records
   * @return number of items that have to be reloaded
   */
  public int getLimit(List<CacheRecordDTO> list) {
    final int weight = list.size() / 100;
    int limit = cacheWarmUpProperties.getMinNumberOfRecords() + (weight * cacheWarmUpProperties.getPercentageFactor());
    final int max = cacheWarmUpProperties.getMaxNumberOfRecords();
    return Math.min(limit, max);
  }

  public CacheRecordDTO mapToCacheRecord(final SMUsageStatistics statistic) {
    final int totalNumberOfUsages = statistic.getTotalNumberOfUsages();
    final CacheNameEntity cacheNameEntity = statistic.getCacheNameEntity();
    final DataProvider provider = DataProvider.of(statistic.getProvider());
    final Holding holding = createHolding(statistic);
    return new CacheRecordDTO(holding, totalNumberOfUsages, cacheNameEntity, provider);
  }

  public Holding createHolding(final SMUsageStatistics statistic) {
    final CacheCategory cacheCategory = Objects.requireNonNull(statistic.getCacheCategory());
    final Holding holding = convertToSpecificHolding(statistic, cacheCategory);
    final HoldingIdentifierType identifierType = Objects.requireNonNull(statistic.getHoldingIdType());
    final HoldingType type = Objects.requireNonNull(statistic.getHoldingType());
    return holding.setValue(ONE).setType(type).setHoldingIdentifier(identifierType);
  }

  public Holding convertToSpecificHolding(SMUsageStatistics statistic, CacheCategory cacheCategory) {
    if (cacheCategory.isMutualFund()) {
      final String id = Objects.requireNonNull(statistic.getHoldingId());
      return new FundSeriesHolding().setFundServCode(id);
    } else if (cacheCategory.isEtf()) {
      final String idRaw = Objects.requireNonNull(statistic.getHoldingId());
      final String[] ids = idRaw.split(GeneralConstants.DELIMITER);
      if (ids.length == 3) {
        return new EtfHolding()
            .setTicker(ids[1].trim())
            .setExchangeCode(ids[2].trim());
      } else {
        return new EtfHolding()
            .setTicker(ids[1]);
      }
    } else if (cacheCategory.isStock()) {
      final String idRaw = Objects.requireNonNull(statistic.getHoldingId());
      final String[] ids = idRaw.split(GeneralConstants.DELIMITER);
      return new StockHolding()
          .setTicker(ids[1].trim())
          .setExchangeCode(ids[2].trim());
    } else if (cacheCategory.isBenchmark()) {
      final String id = Objects.requireNonNull(statistic.getHoldingId());
      return new BenchmarkIndexHolding().setMrStarId(id);
    } else if (cacheCategory.isUsMutualFund()) {
      final String idRaw = Objects.requireNonNull(statistic.getHoldingId());
      final String[] ids = idRaw.split(GeneralConstants.DELIMITER);
      return new UsMutualFundHolding().setTicker(ids[1].trim());
    } else if (cacheCategory.isCanadaPooledFund()) {
      final String idRaw = Objects.requireNonNull(statistic.getHoldingId());
      final String[] ids = idRaw.split(GeneralConstants.DELIMITER);
      return new CanadaPooledFundHolding().setMorningstarId(ids[1].trim());
    } else if (cacheCategory.isCanadaHedgeFund()) {
      final String idRaw = Objects.requireNonNull(statistic.getHoldingId());
      final String[] ids = idRaw.split(GeneralConstants.DELIMITER);
      return new CanadaHedgeFundHolding().setMorningstarId(ids[1].trim());
    } else if (cacheCategory.isFixedIncome()) {
      final String idRaw = Objects.requireNonNull(statistic.getHoldingId());
      final String[] ids = idRaw.split(GeneralConstants.DELIMITER);
      return new FixedIncomeHolding().setIdentifier(ids[1].trim());
    } else if (cacheCategory.isSeparatelyManagedAccount()) {
      final String idRaw = Objects.requireNonNull(statistic.getHoldingId());
      final String[] ids = idRaw.split(GeneralConstants.DELIMITER);
      return new SmaHolding().setIdentifier(ids[1].trim());
    } else if (cacheCategory.isPagGuidedPortfolio()) {
      final String idRaw = Objects.requireNonNull(statistic.getHoldingId());
      final String[] ids = idRaw.split(GeneralConstants.DELIMITER);
      return new PagHolding().setIdentifier(ids[1].trim());
    }
    throw new GeneralRuntimeException("Have not yet implemented the logic for: " + cacheCategory.name());
  }

  @Override
  public CacheWarmUpPort.SchedulerRunInfo cacheWarmUpSchedulerRunCheck() {
    final RCacheWarmUpDate rCacheWarmUpDate = cacheWarmUpSchedulerDateRedisRepository.findAllByPrefixEnv()
        .stream()
        .findFirst()
        .orElse(new RCacheWarmUpDate());

    final boolean wasRunInLast24Hours = lastRunOfCacheWarmUpWasLessThan24HoursAgo(rCacheWarmUpDate);
    return new CacheWarmUpPort.SchedulerRunInfo(wasRunInLast24Hours, rCacheWarmUpDate.getZonedDateTime());
  }

  private boolean lastRunOfCacheWarmUpWasLessThan24HoursAgo(final RCacheWarmUpDate rCacheWarmUpDate) {
    return Objects.nonNull(rCacheWarmUpDate.getZonedDateTime())
        && Duration.between(ZonedDateTime.now(), rCacheWarmUpDate.getZonedDateTime()).toHours() > -24;
  }

}
