package com.fintex.ce.service.impl.cache.statistic;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.HoldingIdentifierType;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.config.enumeration.cache.CacheCategory;
import com.fintex.ce.config.enumeration.cache.CacheNameEntity;
import com.fintex.ce.config.properties.CacheWarmUpProperties;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.cache.CacheRecordDTO;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.PagHolding;
import com.fintex.ce.dto.holding.SmaHolding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.model.jdbc.SMUsageStatistics;
import com.fintex.ce.model.redis.RCacheWarmUpDate;
import com.fintex.ce.repository.jdbc.FASUsageStatisticsRepo;
import com.fintex.ce.repository.redis.CacheWarmUpSchedulerDateRedisRepository;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.impl.cache.statistic.CacheWarmUpServiceImpl.SchedulerRunInfoDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static com.fintex.ce.config.constant.GeneralConstants.DELIMITER;
import static com.fintex.ce.util.PortfolioUtils.calculateInitialPortfolioWeight;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CacheWarmUpServiceImplTest {

    @Test
    void convertToSpecificHolding_mutualFund() {
        //SETUP
        final CacheWarmUpServiceImpl target = mock(CacheWarmUpServiceImpl.class);

        final SMUsageStatistics statistic = mock(SMUsageStatistics.class);

        final String holdingId = "FUND";
        when(statistic.getHoldingId()).thenReturn(holdingId);

        doCallRealMethod().when(target).convertToSpecificHolding(any(), any());
        //ACT
        final Holding actual = target.convertToSpecificHolding(statistic, CacheCategory.CANADA_MUTUAL_FUNDS);

        //VERIFY
        Assertions.assertEquals(new FundSeriesHolding().setFundServCode(holdingId), actual);
    }

    @Test
    void convertToSpecificHolding_etf() {
        //SETUP
        final CacheWarmUpServiceImpl target = mock(CacheWarmUpServiceImpl.class);

        final SMUsageStatistics statistic = mock(SMUsageStatistics.class);

        final String holdingId = "FUND";
        when(statistic.getHoldingId()).thenReturn(HoldingType.US_ETF + DELIMITER + holdingId);

        doCallRealMethod().when(target).convertToSpecificHolding(any(), any());
        //ACT
        final Holding actual = target.convertToSpecificHolding(statistic, CacheCategory.ETF);

        //VERIFY
        Assertions.assertEquals(new EtfHolding().setTicker(holdingId), actual);
    }

    @Test
    void convertToSpecificHolding_etf2() {
        //SETUP
        final CacheWarmUpServiceImpl target = mock(CacheWarmUpServiceImpl.class);

        final SMUsageStatistics statistic = mock(SMUsageStatistics.class);

        final String holdingId = "FUND";
        final String exchangeId = "12";
        when(statistic.getHoldingId()).thenReturn(HoldingType.US_ETF + DELIMITER + holdingId + DELIMITER + exchangeId);

        doCallRealMethod().when(target).convertToSpecificHolding(any(), any());
        //ACT
        final Holding actual = target.convertToSpecificHolding(statistic, CacheCategory.ETF);

        //VERIFY
        assertEquals(new EtfHolding().setTicker(holdingId).setExchangeCode(exchangeId), actual);
    }

    @Test
    void convertToSpecificHolding_stock() {
        //SETUP
        final CacheWarmUpServiceImpl target = mock(CacheWarmUpServiceImpl.class);

        final SMUsageStatistics statistic = mock(SMUsageStatistics.class);

        final String holdingId = "FUND";
        final String exchangeId = "12";
        when(statistic.getHoldingId()).thenReturn(HoldingType.CANADA_STOCKS + DELIMITER + holdingId + DELIMITER + exchangeId);

        doCallRealMethod().when(target).convertToSpecificHolding(any(), any());
        //ACT
        final Holding actual = target.convertToSpecificHolding(statistic, CacheCategory.STOCKS);

        //VERIFY
        Assertions.assertEquals(new StockHolding().setTicker(holdingId).setExchangeCode(exchangeId), actual);
    }

    @Test
    void convertToSpecificHolding_benchmark() {
        //SETUP
        final CacheWarmUpServiceImpl target = mock(CacheWarmUpServiceImpl.class);

        final SMUsageStatistics statistic = mock(SMUsageStatistics.class);

        final String holdingId = "FUND";
        when(statistic.getHoldingId()).thenReturn(holdingId);

        doCallRealMethod().when(target).convertToSpecificHolding(any(), any());
        //ACT
        final Holding actual = target.convertToSpecificHolding(statistic, CacheCategory.BENCHMARK_INDEXES);

        //VERIFY
        Assertions.assertEquals(new BenchmarkIndexHolding().setMrStarId(holdingId), actual);
    }

    @Test
    void convertToSpecificHolding_fixedIncome() {
        //SETUP
        final CacheWarmUpServiceImpl target = mock(CacheWarmUpServiceImpl.class);

        final SMUsageStatistics statistic = mock(SMUsageStatistics.class);

        final String holdingId = "FIXED_INCOME-adpNumber";
        when(statistic.getHoldingId()).thenReturn(holdingId);

        doCallRealMethod().when(target).convertToSpecificHolding(any(), any());

        //ACT
        final Holding actual = target.convertToSpecificHolding(statistic, CacheCategory.FIXED_INCOME);

        //VERIFY
        Assertions.assertEquals(new FixedIncomeHolding().setIdentifier("adpNumber"), actual);
    }

    @Test
    void convertToSpecificHolding_separatelyManagedAccount() {
        //SETUP
        final CacheWarmUpServiceImpl target = mock(CacheWarmUpServiceImpl.class);

        final SMUsageStatistics statistic = mock(SMUsageStatistics.class);

        final String holdingId = "SEPARATELY_MANAGED_ACCOUNT-identifier";
        when(statistic.getHoldingId()).thenReturn(holdingId);

        doCallRealMethod().when(target).convertToSpecificHolding(any(), any());

        //ACT
        final Holding actual = target.convertToSpecificHolding(statistic, CacheCategory.SEPARATELY_MANAGED_ACCOUNT);

        //VERIFY
        Assertions.assertEquals(new SmaHolding().setIdentifier("identifier"), actual);
    }

    @Test
    void convertToSpecificHolding_pagGuidedPortfolio() {
        //SETUP
        final CacheWarmUpServiceImpl target = mock(CacheWarmUpServiceImpl.class);

        final SMUsageStatistics statistic = mock(SMUsageStatistics.class);

        final String holdingId = "PAG_GUIDED_PORTFOLIO-identifier";
        when(statistic.getHoldingId()).thenReturn(holdingId);

        doCallRealMethod().when(target).convertToSpecificHolding(any(), any());

        //ACT
        final Holding actual = target.convertToSpecificHolding(statistic, CacheCategory.PAG_GUIDED_PORTFOLIO);

        //VERIFY
        Assertions.assertEquals(new PagHolding().setIdentifier("identifier"), actual);
    }

    @Test
    void createHolding_verifyConvertToSpecificHolding() {
        //SETUP
        final CacheWarmUpServiceImpl target = mock(CacheWarmUpServiceImpl.class);

        final SMUsageStatistics statistic = mock(SMUsageStatistics.class);

        when(statistic.getCacheCategory()).thenReturn(CacheCategory.ETF);
        when(statistic.getHoldingIdType()).thenReturn(HoldingIdentifierType.TICKER);
        when(statistic.getHoldingType()).thenReturn(HoldingType.US_ETF);

        when(target.convertToSpecificHolding(any(), any())).thenReturn(new BenchmarkIndexHolding());

        doCallRealMethod().when(target).createHolding(any());
        //ACT
        target.createHolding(statistic);

        //VERIFY
        verify(target).convertToSpecificHolding(statistic, CacheCategory.ETF);
    }

    @Test
    void createHolding_checkResult() {
        //SETUP
        final CacheWarmUpServiceImpl target = mock(CacheWarmUpServiceImpl.class);

        final SMUsageStatistics statistic = mock(SMUsageStatistics.class);

        when(statistic.getCacheCategory()).thenReturn(CacheCategory.ETF);
        when(statistic.getHoldingIdType()).thenReturn(HoldingIdentifierType.TICKER);
        when(statistic.getHoldingType()).thenReturn(HoldingType.US_ETF);

        when(target.convertToSpecificHolding(any(), any())).thenReturn(new BenchmarkIndexHolding());

        doCallRealMethod().when(target).createHolding(any());
        //ACT
        final Holding actual = target.createHolding(statistic);

        //VERIFY
        final Holding expected = new BenchmarkIndexHolding()
                .setHoldingIdentifier(HoldingIdentifierType.TICKER).setType(HoldingType.US_ETF).setValue(BigDecimal.ONE);
        assertEquals(expected, actual);
    }

    @Test
    void mapToCacheRecord_verifyCreateHolding() {
        //SETUP
        final CacheWarmUpServiceImpl target = mock(CacheWarmUpServiceImpl.class);

        final SMUsageStatistics statistic = mock(SMUsageStatistics.class);

        when(statistic.getTotalNumberOfUsages()).thenReturn(100);
        when(statistic.getCacheNameEntity()).thenReturn(CacheNameEntity.ASSET_ALLOCATION);
        when(statistic.getProvider()).thenReturn(DataProvider.EAGLE.name());

        when(target.createHolding(any())).thenReturn(new BenchmarkIndexHolding());

        doCallRealMethod().when(target).mapToCacheRecord(any());
        //ACT
        target.mapToCacheRecord(statistic);

        //VERIFY
        verify(target).createHolding(statistic);
    }

    @Test
    void mapToCacheRecord_checkResult() {
        //SETUP
        final CacheWarmUpServiceImpl target = mock(CacheWarmUpServiceImpl.class);

        final SMUsageStatistics statistic = mock(SMUsageStatistics.class);

        when(statistic.getTotalNumberOfUsages()).thenReturn(100);
        when(statistic.getCacheNameEntity()).thenReturn(CacheNameEntity.ASSET_ALLOCATION);
        when(statistic.getProvider()).thenReturn(DataProvider.EAGLE.name());

        final BenchmarkIndexHolding h = new BenchmarkIndexHolding().setMrStarId("23");
        when(target.createHolding(any())).thenReturn(h);

        doCallRealMethod().when(target).mapToCacheRecord(any());
        //ACT
        final CacheRecordDTO actual = target.mapToCacheRecord(statistic);

        //VERIFY
        final CacheRecordDTO expected = new CacheRecordDTO(h, 100, CacheNameEntity.ASSET_ALLOCATION, DataProvider.EAGLE);
        assertEquals(expected, actual);
    }

    @Test
    void getLimit_checkResult() {
        //SETUP
        final MultipleCacheStorageAbstract storage = mock(MultipleCacheStorageAbstract.class);
        final var fasUsageStatisticsRepo = mock(FASUsageStatisticsRepo.class);
        final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
        final var cacheWarmUpProperties = mock(CacheWarmUpProperties.class);
        final var cacheManager = mock(CacheManager.class);
        final var coreRedisCacheRepositories = mock(List.class);
        final var redisConnectionFactory = mock(RedisConnectionFactory.class);
        final var cacheKeyPrefix = mock(CacheKeyPrefix.class);
        final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
                .useConstructor(fasUsageStatisticsRepo, List.of(storage), cacheWarmUpProperties, cacheWarmUpSchedulerDateRedisRepository,
                        coreRedisCacheRepositories, cacheManager, redisConnectionFactory, cacheKeyPrefix));

        final List list = mock(List.class);

        when(list.size()).thenReturn(200);
        when(cacheWarmUpProperties.getPercentageFactor()).thenReturn(25);
        when(cacheWarmUpProperties.getMinNumberOfRecords()).thenReturn(10);
        when(cacheWarmUpProperties.getMaxNumberOfRecords()).thenReturn(100);

        doCallRealMethod().when(sut).getLimit(any());
        //ACT
        final int actual = sut.getLimit(list);

        //VERIFY
        assertEquals(60, actual);
    }

    @Test
    void getLimit_checkResult2() {
        //SETUP
        final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
        final CacheWarmUpProperties cacheWarmUpProperties = mock(CacheWarmUpProperties.class);
        final var cacheManager = mock(CacheManager.class);
        final var coreRedisCacheRepositories = mock(List.class);
        final var redisConnectionFactory = mock(RedisConnectionFactory.class);
        final var cacheKeyPrefix = mock(CacheKeyPrefix.class);
        final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
                .useConstructor(null, null, cacheWarmUpProperties, cacheWarmUpSchedulerDateRedisRepository,
                        coreRedisCacheRepositories, cacheManager, redisConnectionFactory, cacheKeyPrefix));

        final List list = mock(List.class);

        when(list.size()).thenReturn(13);
        when(cacheWarmUpProperties.getPercentageFactor()).thenReturn(25);
        when(cacheWarmUpProperties.getMinNumberOfRecords()).thenReturn(10);
        when(cacheWarmUpProperties.getMaxNumberOfRecords()).thenReturn(11);

        doCallRealMethod().when(sut).getLimit(any());
        //ACT
        final int actual = sut.getLimit(list);

        //VERIFY
        assertEquals(10, actual);
    }

    @Test
    void limitRecords_verifyGetLimit() {
        //SETUP
        final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
        final CacheWarmUpProperties cacheWarmUpProperties = mock(CacheWarmUpProperties.class);
        final var cacheManager = mock(CacheManager.class);
        final var coreRedisCacheRepositories = mock(List.class);
        final var redisConnectionFactory = mock(RedisConnectionFactory.class);
        final var cacheKeyPrefix = mock(CacheKeyPrefix.class);
        final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
                .useConstructor(null, null, cacheWarmUpProperties, cacheWarmUpSchedulerDateRedisRepository,
                        coreRedisCacheRepositories, cacheManager, redisConnectionFactory, cacheKeyPrefix));

        final CacheRecordDTO r1 = new CacheRecordDTO().setNumberOfUsages(2);

        when(cacheWarmUpProperties.getMinNumberOfRecordUsages()).thenReturn(2);
        when(cacheWarmUpProperties.getMaxNumberOfRecords()).thenReturn(10);

        doCallRealMethod().when(sut).limitRecords(any());
        //ACT
        sut.limitRecords(List.of(r1, new CacheRecordDTO().setNumberOfUsages(1)));

        //VERIFY
        verify(sut).getLimit(List.of(r1));
    }

    @Test
    void limitRecords_checkResult() {
        //SETUP
        final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
        final CacheWarmUpProperties cacheWarmUpProperties = mock(CacheWarmUpProperties.class);
        final var cacheManager = mock(CacheManager.class);
        final var coreRedisCacheRepositories = mock(List.class);
        final var redisConnectionFactory = mock(RedisConnectionFactory.class);
        final var cacheKeyPrefix = mock(CacheKeyPrefix.class);
        final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
                .useConstructor(null, null, cacheWarmUpProperties, cacheWarmUpSchedulerDateRedisRepository,
                        coreRedisCacheRepositories, cacheManager, redisConnectionFactory, cacheKeyPrefix));

        final CacheRecordDTO r = new CacheRecordDTO().setNumberOfUsages(1);
        final CacheRecordDTO r1 = new CacheRecordDTO().setNumberOfUsages(2);
        final CacheRecordDTO r2 = new CacheRecordDTO().setNumberOfUsages(3);
        final CacheRecordDTO r3 = new CacheRecordDTO().setNumberOfUsages(4);

        when(cacheWarmUpProperties.getMinNumberOfRecordUsages()).thenReturn(2);
        when(cacheWarmUpProperties.getMaxNumberOfRecords()).thenReturn(10);

        when(sut.getLimit(any())).thenReturn(2);

        doCallRealMethod().when(sut).limitRecords(any());
        //ACT
        final List<CacheRecordDTO> actual = sut.limitRecords(List.of(r3, r1, r2, r));

        //VERIFY
        assertEquals(List.of(r3, r2), actual);
    }

    @Test
    void limitRecords_checkResult2() {
        //SETUP
        final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
        final CacheWarmUpProperties cacheWarmUpProperties = mock(CacheWarmUpProperties.class);
        final var cacheManager = mock(CacheManager.class);
        final var coreRedisCacheRepositories = mock(List.class);
        final var redisConnectionFactory = mock(RedisConnectionFactory.class);
        final var cacheKeyPrefix = mock(CacheKeyPrefix.class);
        final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
                .useConstructor(null, null, cacheWarmUpProperties, cacheWarmUpSchedulerDateRedisRepository,
                        coreRedisCacheRepositories, cacheManager, redisConnectionFactory, cacheKeyPrefix));

        final CacheRecordDTO r = new CacheRecordDTO().setNumberOfUsages(1);
        final CacheRecordDTO r1 = new CacheRecordDTO().setNumberOfUsages(2);
        final CacheRecordDTO r2 = new CacheRecordDTO().setNumberOfUsages(3);
        final CacheRecordDTO r3 = new CacheRecordDTO().setNumberOfUsages(4);

        when(cacheWarmUpProperties.getMinNumberOfRecordUsages()).thenReturn(2);
        when(cacheWarmUpProperties.getMinNumberOfRecords()).thenReturn(3);

        when(sut.getLimit(any())).thenReturn(2);

        doCallRealMethod().when(sut).limitRecords(any());
        //ACT
        final List<CacheRecordDTO> actual = sut.limitRecords(List.of(r3, r1, r2, r));

        //VERIFY
        assertEquals(List.of(r3, r2, r1), actual);
    }

    @Test
    void selectRecords_verifyFindAll() {
        //SETUP
        final var fasUsageStatisticsRepo = mock(FASUsageStatisticsRepo.class);
        final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
        final var cacheWarmUpProperties = mock(CacheWarmUpProperties.class);
        final var cacheManager = mock(CacheManager.class);
        final var coreRedisCacheRepositories = mock(List.class);
        final var redisConnectionFactory = mock(RedisConnectionFactory.class);
        final var cacheKeyPrefix = mock(CacheKeyPrefix.class);
        final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
                .useConstructor(fasUsageStatisticsRepo, null, cacheWarmUpProperties, cacheWarmUpSchedulerDateRedisRepository,
                        coreRedisCacheRepositories, cacheManager, redisConnectionFactory, cacheKeyPrefix));

        when(fasUsageStatisticsRepo.findAll()).thenReturn(List.of());

        doCallRealMethod().when(sut).selectRecords();
        //ACT
        sut.selectRecords();

        //VERIFY
        verify(fasUsageStatisticsRepo).findAll();
    }

    @Test
    void selectRecords_verifyMapToCacheRecord() {
        //SETUP
        final var fasUsageStatisticsRepo = mock(FASUsageStatisticsRepo.class);
        final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
        final var cacheWarmUpProperties = mock(CacheWarmUpProperties.class);
        final var cacheManager = mock(CacheManager.class);
        final var coreRedisCacheRepositories = mock(List.class);
        final var redisConnectionFactory = mock(RedisConnectionFactory.class);
        final var cacheKeyPrefix = mock(CacheKeyPrefix.class);
        final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
                .useConstructor(fasUsageStatisticsRepo, null, cacheWarmUpProperties, cacheWarmUpSchedulerDateRedisRepository,
                        coreRedisCacheRepositories, cacheManager, redisConnectionFactory, cacheKeyPrefix));

        final SMUsageStatistics sm = new SMUsageStatistics().setDay0Count(1);
        when(fasUsageStatisticsRepo.findAll()).thenReturn(List.of(sm));

        doCallRealMethod().when(sut).selectRecords();
        //ACT
        sut.selectRecords();

        //VERIFY
        verify(sut).mapToCacheRecord(sm);
    }

    @Test
    void selectRecords_verifyLimitRecords() {
        //SETUP
        final var fasUsageStatisticsRepo = mock(FASUsageStatisticsRepo.class);
        final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
        final var cacheWarmUpProperties = mock(CacheWarmUpProperties.class);
        final var cacheManager = mock(CacheManager.class);
        final var coreRedisCacheRepositories = mock(List.class);
        final var redisConnectionFactory = mock(RedisConnectionFactory.class);
        final var cacheKeyPrefix = mock(CacheKeyPrefix.class);
        final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
                .useConstructor(fasUsageStatisticsRepo, null, cacheWarmUpProperties, cacheWarmUpSchedulerDateRedisRepository,
                        coreRedisCacheRepositories, cacheManager, redisConnectionFactory, cacheKeyPrefix));

        final SMUsageStatistics sm = new SMUsageStatistics().setDay0Count(1);
        final CacheRecordDTO record = new CacheRecordDTO().setNumberOfUsages(23);

        when(fasUsageStatisticsRepo.findAll()).thenReturn(List.of(sm));
        when(sut.mapToCacheRecord(any())).thenReturn(record);

        doCallRealMethod().when(sut).selectRecords();
        //ACT
        sut.selectRecords();

        //VERIFY
        verify(sut).limitRecords(List.of(record));
    }

    @Test
    void selectRecords_checkResult() {
        //SETUP
        final var fasUsageStatisticsRepo = mock(FASUsageStatisticsRepo.class);
        final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
        final var cacheWarmUpProperties = mock(CacheWarmUpProperties.class);
        final var cacheManager = mock(CacheManager.class);
        final var coreRedisCacheRepositories = mock(List.class);
        final var redisConnectionFactory = mock(RedisConnectionFactory.class);
        final var cacheKeyPrefix = mock(CacheKeyPrefix.class);
        final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
                .useConstructor(fasUsageStatisticsRepo, null, cacheWarmUpProperties, cacheWarmUpSchedulerDateRedisRepository,
                        coreRedisCacheRepositories, cacheManager, redisConnectionFactory, cacheKeyPrefix));

        final SMUsageStatistics sm = new SMUsageStatistics().setDay0Count(1);
        final CacheRecordDTO record = new CacheRecordDTO().setNumberOfUsages(23);

        when(fasUsageStatisticsRepo.findAll()).thenReturn(List.of(sm));
        when(sut.mapToCacheRecord(any())).thenReturn(record);
        when(sut.limitRecords(any())).thenReturn(List.of(record));

        doCallRealMethod().when(sut).selectRecords();
        //ACT
        final List<CacheRecordDTO> actual = sut.selectRecords();

        //VERIFY
        assertEquals(List.of(record), actual);
    }

    @Test
    void reloadCache_verifyLoad() {
        //SETUP
        final MultipleCacheStorageAbstract storage = mock(MultipleCacheStorageAbstract.class);
        final var fasUsageStatisticsRepo = mock(FASUsageStatisticsRepo.class);
        final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
        final var cacheWarmUpProperties = mock(CacheWarmUpProperties.class);
        final var cacheManager = mock(CacheManager.class);
        final var coreRedisCacheRepositories = mock(List.class);
        final var redisConnectionFactory = mock(RedisConnectionFactory.class);
        final var cacheKeyPrefix = mock(CacheKeyPrefix.class);
        final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
                .useConstructor(fasUsageStatisticsRepo, List.of(storage), cacheWarmUpProperties, cacheWarmUpSchedulerDateRedisRepository,
                        coreRedisCacheRepositories, cacheManager, redisConnectionFactory, cacheKeyPrefix));

        final Holding h1 = new StockHolding().setTicker("23").setValue(BigDecimal.ONE);
        final Holding h2 = new FundSeriesHolding().setFundServCode("F23").setValue(BigDecimal.ONE);
        final CacheRecordDTO r1 = new CacheRecordDTO().setHolding(h1);
        final CacheRecordDTO r2 = new CacheRecordDTO().setProvider(DataProvider.MORNINGSTAR).setHolding(h2);

        when(storage.getCacheNameEntity()).thenReturn(CacheNameEntity.ASSET_ALLOCATION);

        doCallRealMethod().when(sut).reloadCache(any(), any());
        //ACT
        sut.reloadCache(CacheNameEntity.ASSET_ALLOCATION, List.of(r1, r2));

        //VERIFY
        verify(storage).load(List.of(h1, h2), List.of(), List.of(DataProvider.MORNINGSTAR), new ParamHolderDTO(calculateInitialPortfolioWeight(List.of(h1, h2))));
    }

    @Test
    void run_verifySelectRecords() {
        //SETUP
        final MultipleCacheStorageAbstract storage = mock(MultipleCacheStorageAbstract.class);
        final var fasUsageStatisticsRepo = mock(FASUsageStatisticsRepo.class);
        final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
        final var cacheWarmUpProperties = mock(CacheWarmUpProperties.class);
        final var cacheManager = mock(CacheManager.class);
        final var coreRedisCacheRepositories = mock(List.class);
        final var redisConnectionFactory = mock(RedisConnectionFactory.class);
        final var cacheKeyPrefix = mock(CacheKeyPrefix.class);
        final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
                .useConstructor(fasUsageStatisticsRepo, List.of(storage), cacheWarmUpProperties, cacheWarmUpSchedulerDateRedisRepository,
                        coreRedisCacheRepositories, cacheManager, redisConnectionFactory, cacheKeyPrefix));

        doCallRealMethod().when(sut).reloadCache();
        when(sut.selectRecords()).thenReturn(List.of());

        doCallRealMethod().when(sut).run();
        //ACT
        sut.run();

        //VERIFY
        verify(sut).selectRecords();
    }


    @Test
    void run_verifyUpdateDayCountToZeroForDayOfWeek() {
        //SETUP
        final MultipleCacheStorageAbstract storage = mock(MultipleCacheStorageAbstract.class);
        final var fasUsageStatisticsRepo = mock(FASUsageStatisticsRepo.class);
        final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
        final var cacheWarmUpProperties = mock(CacheWarmUpProperties.class);
        final var cacheManager = mock(CacheManager.class);
        final var coreRedisCacheRepositories = mock(List.class);
        final var redisConnectionFactory = mock(RedisConnectionFactory.class);
        final var cacheKeyPrefix = mock(CacheKeyPrefix.class);
        final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
                .useConstructor(fasUsageStatisticsRepo, List.of(storage), cacheWarmUpProperties, cacheWarmUpSchedulerDateRedisRepository,
                        coreRedisCacheRepositories, cacheManager, redisConnectionFactory, cacheKeyPrefix));

        when(sut.selectRecords()).thenReturn(List.of());

        doCallRealMethod().when(sut).run();
        //ACT
        sut.run();

        //VERIFY
        verify(fasUsageStatisticsRepo).updateDayCountToZeroForDayOfWeek(LocalDate.now().getDayOfWeek().ordinal());
    }

    @Test
    void run_verifyReloadCache() {
        //SETUP
        final MultipleCacheStorageAbstract storage = mock(MultipleCacheStorageAbstract.class);
        final var fasUsageStatisticsRepo = mock(FASUsageStatisticsRepo.class);
        final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
        final var cacheWarmUpProperties = mock(CacheWarmUpProperties.class);
        final var cacheManager = mock(CacheManager.class);
        final var coreRedisCacheRepositories = mock(List.class);
        final var redisConnectionFactory = mock(RedisConnectionFactory.class);
        final var cacheKeyPrefix = mock(CacheKeyPrefix.class);
        final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
                .useConstructor(fasUsageStatisticsRepo, List.of(storage), cacheWarmUpProperties, cacheWarmUpSchedulerDateRedisRepository,
                        coreRedisCacheRepositories, cacheManager, redisConnectionFactory, cacheKeyPrefix));

        final CacheRecordDTO r = new CacheRecordDTO().setCacheNameEntity(CacheNameEntity.ASSET_ALLOCATION);
        final CacheRecordDTO r1 = new CacheRecordDTO().setNumberOfUsages(2).setCacheNameEntity(CacheNameEntity.ASSET_ALLOCATION);
        final CacheRecordDTO r_ = new CacheRecordDTO().setCacheNameEntity(CacheNameEntity.MER);
        when(sut.selectRecords()).thenReturn(List.of(r, r1, r_));
        doCallRealMethod().when(sut).reloadCache();

        doCallRealMethod().when(sut).run();
        //ACT
        sut.run();

        //VERIFY
        verify(sut).reloadCache(CacheNameEntity.ASSET_ALLOCATION, List.of(r, r1));
        verify(sut).reloadCache(CacheNameEntity.MER, List.of(r_));
    }

    @Test
    void cacheWarmUpSchedulerRunCheck_checkResultSchedulerRunLessThan10HoursAgo() {
        //SETUP
        final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
        final var cacheManager = mock(CacheManager.class);
        final var coreRedisCacheRepositories = mock(List.class);
        final var redisConnectionFactory = mock(RedisConnectionFactory.class);
        final var cacheKeyPrefix = mock(CacheKeyPrefix.class);
        final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
                .useConstructor(null, null, null, cacheWarmUpSchedulerDateRedisRepository,
                        coreRedisCacheRepositories, cacheManager, redisConnectionFactory, cacheKeyPrefix));

        final ZonedDateTime nowMinus10Hours = ZonedDateTime.now().minusHours(10);
        final RCacheWarmUpDate rCacheWarmUpDate = new RCacheWarmUpDate().setZonedDateTime(nowMinus10Hours);
        final var expected = new SchedulerRunInfoDto().setLastTimeRun(nowMinus10Hours).setRunInLast24Hours(true);

        doReturn(List.of(rCacheWarmUpDate)).when(cacheWarmUpSchedulerDateRedisRepository).findAllByPrefixEnv();

        doCallRealMethod().when(sut).cacheWarmUpSchedulerRunCheck();
        //ACT
        final SchedulerRunInfoDto actual = sut.cacheWarmUpSchedulerRunCheck();

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void cacheWarmUpSchedulerRunCheck_checkResultSchedulerDidntRun() {
        //SETUP
        final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
        final var cacheManager = mock(CacheManager.class);
        final var coreRedisCacheRepositories = mock(List.class);
        final var redisConnectionFactory = mock(RedisConnectionFactory.class);
        final var cacheKeyPrefix = mock(CacheKeyPrefix.class);
        final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
                .useConstructor(null, null, null, cacheWarmUpSchedulerDateRedisRepository,
                        coreRedisCacheRepositories, cacheManager, redisConnectionFactory, cacheKeyPrefix));
        final var expected = new SchedulerRunInfoDto().setRunInLast24Hours(false);

        doReturn(List.of()).when(cacheWarmUpSchedulerDateRedisRepository).findAll();

        doCallRealMethod().when(sut).cacheWarmUpSchedulerRunCheck();
        //ACT
        final SchedulerRunInfoDto actual = sut.cacheWarmUpSchedulerRunCheck();

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void cacheWarmUpSchedulerRunCheck_checkResultSchedulerDidntRunIn24Hours() {
        //SETUP
        final var cacheWarmUpSchedulerDateRedisRepository = mock(CacheWarmUpSchedulerDateRedisRepository.class);
        final var cacheManager = mock(CacheManager.class);
        final var coreRedisCacheRepositories = mock(List.class);
        final var redisConnectionFactory = mock(RedisConnectionFactory.class);
        final var cacheKeyPrefix = mock(CacheKeyPrefix.class);
        final var sut = mock(CacheWarmUpServiceImpl.class, withSettings()
                .useConstructor(null, null, null, cacheWarmUpSchedulerDateRedisRepository,
                        coreRedisCacheRepositories, cacheManager, redisConnectionFactory, cacheKeyPrefix));

        final ZonedDateTime zonedDateTime = ZonedDateTime.now().minusHours(25);
        final RCacheWarmUpDate rCacheWarmUpDate = new RCacheWarmUpDate().setZonedDateTime(zonedDateTime);
        final var expected = new SchedulerRunInfoDto().setLastTimeRun(zonedDateTime).setRunInLast24Hours(false);

        doReturn(List.of(rCacheWarmUpDate)).when(cacheWarmUpSchedulerDateRedisRepository).findAllByPrefixEnv();

        doCallRealMethod().when(sut).cacheWarmUpSchedulerRunCheck();
        //ACT
        final SchedulerRunInfoDto actual = sut.cacheWarmUpSchedulerRunCheck();

        //VERIFY
        assertEquals(expected, actual);
    }

}
