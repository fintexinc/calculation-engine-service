package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.config.enumeration.cache.CacheNameEntity;
import com.fintex.ce.dto.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.core.MultipleSMRepository;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
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

public abstract class ManagementExpenseAbstractCacheStorage<F extends RedisId, C extends RedisId, U extends RedisId, S extends RedisId>
        extends MultipleCacheStorageAbstract<F, C, U, S> {

    protected ManagementExpenseAbstractCacheStorage(final MultipleSMRepository<F, C, U, S> queryRepository,
                                                 final CoreRedisCacheRepository<F> fundCanadaRepo,
                                                 final CoreRedisCacheRepository<C> etfCanadaRepo,
                                                 final CoreRedisCacheRepository<U> etfUsRepo,
                                                 final CacheStatisticService cacheStatisticService,
                                                 final CacheNameEntity cacheNameEntity) {
        super(queryRepository, fundCanadaRepo, etfCanadaRepo, etfUsRepo, null, cacheStatisticService, cacheNameEntity);
    }

    @Override
    public Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> load(final List<Holding> holdings, final List<DataProvider> providers,
                                                                                       final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
        final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> result = new EnumMap<>(HoldingType.class);

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

    public abstract AverageManagementExpenseCalculationDTO mapperForCanadaMutualFund(final Holding holding, final F averageMerEtfCanada);

    public abstract AverageManagementExpenseCalculationDTO mapperForEtfCanada(final Holding holding, final C averageMerEtfCanada);

    public abstract AverageManagementExpenseCalculationDTO mapperForEtfUs(final Holding holding, final U averageMerEtfCanada);

    public abstract AverageManagementExpenseCalculationDTO mapperForUsMutualFund(final Holding holding, final F averageMerUsMutualFund);

    public abstract AverageManagementExpenseCalculationDTO mapperForCanadaHedgeFund(final Holding holding, final F averageMerCanadaHedgeFund);

    public abstract void dataProviderCheckerForCanadaMutualFund(final List<DataProvider> providers, Collection<F> responseFromFds);

    public abstract void dataProviderCheckerForEtfCanada(final List<DataProvider> providers, Collection<C> responseFromFds);

    public abstract void dataProviderCheckerForEtfUs(final List<DataProvider> providers, Collection<U> responseFromFds);

    public abstract void dataProviderCheckerForUsMutualFund(final List<DataProvider> providers, Collection<F> responseFromFds);

    public abstract void dataProviderCheckerForCanadaHedgeFund(final List<DataProvider> providers, Collection<F> responseFromFds);

    <H extends Holding, R extends RedisId> void addHoldingsToResult(
            final List<H> holdings,
            final List<DataProvider> providers,
            final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> result,
            final BiFunction<List<H>, List<DataProvider>, Map<H, R>> fdsCall,
            final BiConsumer<List<DataProvider>, Collection<R>> dataProviderChecker,
            final BiFunction<H, R, AverageManagementExpenseCalculationDTO> mapper
    ) {
        if (CollectionUtils.isEmpty(holdings)) {
            return;
        }
        final Map<H, R> responseMap = fdsCall.apply(holdings, providers);
        dataProviderChecker.accept(providers, responseMap.values());
        final Map<Holding, AverageManagementExpenseCalculationDTO> preBuildMERs = responseMap.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, e -> mapper.apply(e.getKey(), e.getValue())));
        result.put(holdings.get(0).getType(), preBuildMERs);
    }

    protected void addHoldingsToResult(final List<Holding> holdings, final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> resultMap) {
        if (CollectionUtils.isEmpty(holdings)) {
            return;
        }

        final Map<Holding, AverageManagementExpenseCalculationDTO> averageMerCalculationDTOs = holdings.stream()
                .collect(toMap(e -> e, this::mapperForHolding));
        resultMap.put(holdings.get(0).getType(), averageMerCalculationDTOs);
    }

    AverageManagementExpenseCalculationDTO mapperForHolding(final Holding holding) {
        final var result = preBuildAverageMerDto(holding);
        result.setInitialFee(BigDecimal.ZERO);
        result.setModifiedFee(BigDecimal.ZERO);
        return result;
    }

    AverageManagementExpenseCalculationDTO preBuildAverageMerDto(final Holding holding) {
        return new AverageManagementExpenseCalculationDTO()
                .setMarketValue(holding.getValue())
                .setHoldingType(holding.getType());
    }

}
