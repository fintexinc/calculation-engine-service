package com.fintex.ce.adapter.cache;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.constant.CacheNameEntity;
import com.fintex.ce.domain.model.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.cache.entity.core.RedisId;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.graphql.MultipleSMRepository;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import com.fintex.ce.adapter.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticService;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_STOCKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_STOCKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;

/**
 * Abstract cache storage for management expense data.
 *
 * @param <F>
 *          Fund Canada domain model type (also used for UsMutualFund, CanadaHedgeFund)
 * @param <C>
 *          ETF Canada domain model type
 * @param <U>
 *          ETF US domain model type
 * @param <S>
 *          Stock domain model type
 * @param <T>
 *          Redis entity type (extends RedisId, used for Redis storage)
 */
public abstract class ManagementExpenseAbstractCacheStorage<F, C, U, S, T extends RedisId>
    extends
      MultipleCacheStorageAbstract<F, C, U, S, T> {

  protected ManagementExpenseAbstractCacheStorage(final MultipleSMRepository<F, C, U, S> smRepo,
      final CacheEntityMapper<F, T> fundMapper,
      final CacheEntityMapper<C, T> etfCanadaMapper,
      final CacheEntityMapper<U, T> etfUsMapper,
      final CacheEntityMapper<S, T> stockMapper,
      final CoreRedisCacheRepository<T> fundCanadaRepo,
      final CoreRedisCacheRepository<T> etfCanadaRepo,
      final CoreRedisCacheRepository<T> etfUsRepo,
      final CacheStatisticService cacheStatisticService,
      final CacheNameEntity cacheNameEntity) {
    super(smRepo, fundMapper, etfCanadaMapper, etfUsMapper, stockMapper, fundCanadaRepo, etfCanadaRepo, etfUsRepo, null,
        cacheStatisticService, cacheNameEntity);
  }

  @Override
  public Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> load(final List<Holding> holdings,
      final List<DataProvider> providers,
      final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
    final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> result = new EnumMap<>(
        HoldingType.class);

    addHoldingsToResult(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), providers, result,
        this::loadBenchOfFundCanada, this::dataProviderCheckerForCanadaMutualFund, this::mapperForCanadaMutualFund);
    addHoldingsToResult(filterHoldings(holdings, CANADA_ETF_PREDICATE), providers, result,
        this::loadForBenchOfEtfCanada, this::dataProviderCheckerForEtfCanada, this::mapperForEtfCanada);
    addHoldingsToResult(filterHoldings(holdings, US_ETF_PREDICATE), providers, result,
        this::loadForBenchOfEtfUs, this::dataProviderCheckerForEtfUs, this::mapperForEtfUs);
    addHoldingsToResult(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), providers, result,
        this::loadCanadaHedgeFunds, this::dataProviderCheckerForCanadaHedgeFund, this::mapperForCanadaHedgeFund);
    addHoldingsToResult(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), providers, result,
        this::loadUsMutualFunds, this::dataProviderCheckerForUsMutualFund, this::mapperForUsMutualFund);

    addHoldingsToResult(filterHoldings(holdings, CASH_PREDICATE), result);
    addHoldingsToResult(filterHoldings(holdings, US_STOCKS_PREDICATE), result);
    addHoldingsToResult(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), result);
    addHoldingsToResult(filterHoldings(holdings, CANADA_STOCKS_PREDICATE), result);
    addHoldingsToResult(filterHoldings(holdings, GIC_PREDICATE), result);

    return result;
  }

  public abstract AverageManagementExpenseCalculationDTO mapperForCanadaMutualFund(final Holding holding,
      final F averageMerFundCanada);

  public abstract AverageManagementExpenseCalculationDTO mapperForEtfCanada(final Holding holding,
      final C averageMerEtfCanada);

  public abstract AverageManagementExpenseCalculationDTO mapperForEtfUs(final Holding holding, final U averageMerEtfUs);

  public abstract AverageManagementExpenseCalculationDTO mapperForUsMutualFund(final Holding holding,
      final F averageMerUsMutualFund);

  public abstract AverageManagementExpenseCalculationDTO mapperForCanadaHedgeFund(final Holding holding,
      final F averageMerCanadaHedgeFund);

  public abstract void dataProviderCheckerForCanadaMutualFund(final List<DataProvider> providers,
      Collection<F> responseFromFds);

  public abstract void dataProviderCheckerForEtfCanada(final List<DataProvider> providers,
      Collection<C> responseFromFds);

  public abstract void dataProviderCheckerForEtfUs(final List<DataProvider> providers, Collection<U> responseFromFds);

  public abstract void dataProviderCheckerForUsMutualFund(final List<DataProvider> providers,
      Collection<F> responseFromFds);

  public abstract void dataProviderCheckerForCanadaHedgeFund(final List<DataProvider> providers,
      Collection<F> responseFromFds);

  public <H extends Holding, D> void addHoldingsToResult(
      final List<H> holdings,
      final List<DataProvider> providers,
      final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> result,
      final BiFunction<List<H>, List<DataProvider>, Map<H, D>> fdsCall,
      final BiConsumer<List<DataProvider>, Collection<D>> dataProviderChecker,
      final BiFunction<H, D, AverageManagementExpenseCalculationDTO> domainMapper) {
    if (CollectionUtils.isEmpty(holdings)) {
      return;
    }
    final Map<H, D> responseMap = fdsCall.apply(holdings, providers);
    dataProviderChecker.accept(providers, responseMap.values());
    final Map<Holding, AverageManagementExpenseCalculationDTO> preBuildMERs = responseMap.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> domainMapper.apply(e.getKey(), e.getValue())));
    result.put(holdings.get(0).getType(), preBuildMERs);
  }

  public void addHoldingsToResult(final List<Holding> holdings,
      final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> resultMap) {
    if (CollectionUtils.isEmpty(holdings)) {
      return;
    }

    final Map<Holding, AverageManagementExpenseCalculationDTO> averageMerCalculationDTOs = holdings.stream()
        .collect(toMap(e -> e, this::mapperForHolding));
    resultMap.put(holdings.get(0).getType(), averageMerCalculationDTOs);
  }

  public AverageManagementExpenseCalculationDTO mapperForHolding(final Holding holding) {
    final var result = preBuildAverageMerDto(holding);
    result.setInitialFee(BigDecimal.ZERO);
    result.setModifiedFee(BigDecimal.ZERO);
    return result;
  }

  public AverageManagementExpenseCalculationDTO preBuildAverageMerDto(final Holding holding) {
    return new AverageManagementExpenseCalculationDTO()
        .setMarketValue(holding.getValue())
        .setHoldingType(holding.getType());
  }

}
