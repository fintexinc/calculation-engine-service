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
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import com.fintex.ce.adapter.cache.core.CacheStorageAbstract;
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
 * @param <T>
 *          Domain model type
 * @param <R>
 *          Redis entity type (extends RedisId, used for Redis storage)
 */
public abstract class ManagementExpenseAbstractCacheStorage<T, R extends RedisId>
    extends
      CacheStorageAbstract<T, R, Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>>> {

  protected ManagementExpenseAbstractCacheStorage(final SecurityDataPort<T> securityDataPort,
      final CacheEntityMapper<T, R> mapper,
      final CoreRedisCacheRepository<R> cacheRepo,
      final CacheNameEntity cacheNameEntity) {
    super(securityDataPort, mapper, cacheRepo, cacheNameEntity);
  }

  @Override
  public Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> load(final List<? extends Holding> holdings,
      final List<DataProvider> providers,
      final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
    final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> result = new EnumMap<>(
        HoldingType.class);

    addHoldingsToResult(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), providers, result,
        (h, p) -> loadBenchOfFundCanada(h, p), this::dataProviderCheckerForCanadaMutualFund, this::mapperForCanadaMutualFund);
    addHoldingsToResult(filterHoldings(holdings, CANADA_ETF_PREDICATE), providers, result,
        (h, p) -> loadForBenchOfEtfCanada(h, p), this::dataProviderCheckerForEtfCanada, this::mapperForEtfCanada);
    addHoldingsToResult(filterHoldings(holdings, US_ETF_PREDICATE), providers, result,
        (h, p) -> loadForBenchOfEtfUs(h, p), this::dataProviderCheckerForEtfUs, this::mapperForEtfUs);
    addHoldingsToResult(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), providers, result,
        (h, p) -> loadCanadaHedgeFunds(h, p), this::dataProviderCheckerForCanadaHedgeFund, this::mapperForCanadaHedgeFund);
    addHoldingsToResult(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), providers, result,
        (h, p) -> loadUsMutualFunds(h, p), this::dataProviderCheckerForUsMutualFund, this::mapperForUsMutualFund);

    addHoldingsToResult(filterHoldings(holdings, CASH_PREDICATE), result);
    addHoldingsToResult(filterHoldings(holdings, US_STOCKS_PREDICATE), result);
    addHoldingsToResult(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), result);
    addHoldingsToResult(filterHoldings(holdings, CANADA_STOCKS_PREDICATE), result);
    addHoldingsToResult(filterHoldings(holdings, GIC_PREDICATE), result);

    return result;
  }

  public abstract AverageManagementExpenseCalculationDTO mapperForCanadaMutualFund(final Holding holding,
      final T averageMerFundCanada);

  public abstract AverageManagementExpenseCalculationDTO mapperForEtfCanada(final Holding holding,
      final T averageMerEtfCanada);

  public abstract AverageManagementExpenseCalculationDTO mapperForEtfUs(final Holding holding, final T averageMerEtfUs);

  public abstract AverageManagementExpenseCalculationDTO mapperForUsMutualFund(final Holding holding,
      final T averageMerUsMutualFund);

  public abstract AverageManagementExpenseCalculationDTO mapperForCanadaHedgeFund(final Holding holding,
      final T averageMerCanadaHedgeFund);

  public abstract void dataProviderCheckerForCanadaMutualFund(final List<DataProvider> providers,
      Collection<T> responseFromFds);

  public abstract void dataProviderCheckerForEtfCanada(final List<DataProvider> providers,
      Collection<T> responseFromFds);

  public abstract void dataProviderCheckerForEtfUs(final List<DataProvider> providers, Collection<T> responseFromFds);

  public abstract void dataProviderCheckerForUsMutualFund(final List<DataProvider> providers,
      Collection<T> responseFromFds);

  public abstract void dataProviderCheckerForCanadaHedgeFund(final List<DataProvider> providers,
      Collection<T> responseFromFds);

  public <H extends Holding> void addHoldingsToResult(
      final List<H> holdings,
      final List<DataProvider> providers,
      final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> result,
      final BiFunction<List<H>, List<DataProvider>, Map<H, T>> fdsCall,
      final BiConsumer<List<DataProvider>, Collection<T>> dataProviderChecker,
      final BiFunction<H, T, AverageManagementExpenseCalculationDTO> domainMapper) {
    if (CollectionUtils.isEmpty(holdings)) {
      return;
    }
    final Map<H, T> responseMap = fdsCall.apply(holdings, providers);
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
