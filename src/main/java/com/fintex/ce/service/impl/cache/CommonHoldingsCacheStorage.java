package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.config.enumeration.cache.CacheNameEntity;
import com.fintex.ce.dto.CommonHoldingsDTO;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.CashHolding;
import com.fintex.ce.dto.holding.GicHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.exception.GeneralRuntimeException;
import com.fintex.ce.exception.notification.pattern.Notification;
import com.fintex.ce.model.redis.topcommonholdings.RCommonHoldings;
import com.fintex.ce.model.redis.topcommonholdings.RCommonHoldingsStock;
import com.fintex.ce.repository.graphql.query.CommonHoldingsSMRepository;
import com.fintex.ce.repository.redis.commonholdings.CommonHoldingsRepository;
import com.fintex.ce.repository.redis.commonholdings.CommonHoldingsStockRepository;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.impl.calculation.CommonHoldingsServiceImpl;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;

@Service
public class CommonHoldingsCacheStorage
        extends MultipleCacheStorageAbstract<RCommonHoldings, RCommonHoldings, RCommonHoldings, RCommonHoldingsStock> {

    Set<String> firstLvlRecursionTypes;
    private static final String ABSENT_STOCK_HOLDING = "There is no corresponding stock holding for that holding:";
    private static final String CASH_GIC_ACCUMULATE_TYPE = "B";
    private static final String CASH_NAME = "Cash";

    @Autowired
    public CommonHoldingsCacheStorage(@Value("#{'${default.top-common-holdings.recursion-types}'.split(',')}") Set<String> firstLvlRecursionTypes,
                                      CommonHoldingsSMRepository fdsRepo,
                                      CommonHoldingsRepository fundCanadaCacheRepo,
                                      CommonHoldingsRepository etfCanadaCacheRepo,
                                      CommonHoldingsRepository etfUsCacheRepo,
                                      CommonHoldingsStockRepository stockRepository,
                                      CacheStatisticService cacheStatisticService
    ) {
        super(
                fdsRepo, fundCanadaCacheRepo, etfCanadaCacheRepo,
                etfUsCacheRepo, stockRepository, cacheStatisticService, CacheNameEntity.TOP_COMMON_HOLDINGS
        );
        this.firstLvlRecursionTypes = firstLvlRecursionTypes;
    }

    @Override
    public Map<Holding, List<CommonHoldingsDTO>> load(List<Holding> holdings, List<DataProvider> providers,
                                                      List<Warning> warnings, ParamHolderDTO paramHolderDTO) {
        var notification = new Notification();
        Map<Holding, List<CommonHoldingsDTO>> result = new HashMap<>();

        var canadaEtfs = loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of());
        var usEtfs = loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of());
        var canadaFundServs = loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of());
        var benchmarks = loadForBenchOfBenchmarks(filterHoldings(holdings, BENCHMARKS_PREDICATE), List.of());
        var canadaPooledFunds = loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of());
        var canadaHedgeFunds = loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of());
        var usMutualFunds = loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of());

        validate(canadaEtfs, warnings, notification);
        validate(usEtfs, warnings, notification);
        validate(canadaFundServs, warnings, notification);
        validate(benchmarks, warnings, notification);
        validate(canadaPooledFunds, warnings, notification);
        validate(canadaHedgeFunds, warnings, notification);
        validate(usMutualFunds, warnings, notification);
        notification.ifAnyErrorThrowException();

        result.putAll(mapForNoneStock(canadaEtfs));
        result.putAll(mapForNoneStock(usEtfs));
        result.putAll(mapForNoneStock(canadaFundServs));
        result.putAll(mapForNoneStock(benchmarks));
        result.putAll(mapForNoneStock(canadaPooledFunds));
        result.putAll(mapForNoneStock(canadaHedgeFunds));
        result.putAll(mapForNoneStock(usMutualFunds));
        result.putAll(mapForStock(loadForBenchOfStock(filterHoldings(holdings, STOCK_PREDICATE), List.of()), paramHolderDTO));
        result.putAll(mapForCash(holdings));
        result.putAll(mapForGic(holdings));
        return result;
    }

    Map<Holding, List<CommonHoldingsDTO>> mapForCash(final List<Holding> holdings) {
        Map<Holding, List<CommonHoldingsDTO>> result = new HashMap<>();
        final List<CashHolding> cashHoldings = filterHoldings(holdings, CASH_PREDICATE);
        if (cashHoldings.isEmpty()) {
            return result;
        }

        cashHoldings.forEach(h -> {
            final CommonHoldingsDTO commonHoldingsDTO = new CommonHoldingsDTO();
            commonHoldingsDTO.setUuid(UUID.randomUUID());
            commonHoldingsDTO.setName(CASH_NAME);
            commonHoldingsDTO.setType(CASH_GIC_ACCUMULATE_TYPE);
            commonHoldingsDTO.setValue(BigDecimal.ONE);
            result.put(h, Collections.singletonList(commonHoldingsDTO));
        });

        return result;
    }

    Map<Holding, List<CommonHoldingsDTO>> mapForGic(final List<Holding> holdings) {
        Map<Holding, List<CommonHoldingsDTO>> result = new HashMap<>();
        final List<GicHolding> gicHoldings = filterHoldings(holdings, GIC_PREDICATE);
        if (gicHoldings.isEmpty()) {
            return result;
        }

        gicHoldings.forEach(h -> {
            final CommonHoldingsDTO commonHoldingsDTO = new CommonHoldingsDTO();
            commonHoldingsDTO.setUuid(UUID.randomUUID());
            commonHoldingsDTO.setName(h.getName());
            commonHoldingsDTO.setType(CASH_GIC_ACCUMULATE_TYPE);
            commonHoldingsDTO.setValue(BigDecimal.ONE);
            result.put(h, Collections.singletonList(commonHoldingsDTO));
        });

        return result;
    }

    <H extends Holding> Map<Holding, List<CommonHoldingsDTO>> mapForStock(Map<H, RCommonHoldingsStock> stockHoldings,
                                                                          ParamHolderDTO paramHolderDTO) {
        return stockHoldings.entrySet().stream().collect(toMap(
                Map.Entry::getKey,
                e -> List.of(initializeStockCommonHoldingsDTO(e, paramHolderDTO)))
        );
    }

    <H extends Holding> CommonHoldingsDTO initializeStockCommonHoldingsDTO(Map.Entry<H, RCommonHoldingsStock> stockHolding,
                                                                           ParamHolderDTO paramHolderDTO) {
        return new CommonHoldingsDTO(
                stockHolding.getValue().getCompanyName(),
                CommonHoldingsServiceImpl.EQUITY_TYPE,
                calculateStockHoldingValue(stockHolding, paramHolderDTO),
                stockHolding.getValue().getTicker(),
                stockHolding.getValue().getExchangeCode()
        );
    }

    <H extends Holding> BigDecimal calculateStockHoldingValue(Map.Entry<H, RCommonHoldingsStock> stockHolding,
                                                              ParamHolderDTO paramHolderDTO) {
        return paramHolderDTO.getAllocations().entrySet().stream()
                .filter(e -> stockHolding.getKey().generateUserIdentifier().equals(e.getKey().generateUserIdentifier()))
                .findFirst()
                .orElseThrow(() -> new GeneralRuntimeException(ABSENT_STOCK_HOLDING + stockHolding))
                .getValue();
    }

    <H extends Holding> Map<Holding, List<CommonHoldingsDTO>> mapForNoneStock(Map<H, RCommonHoldings> holdings) {
        if (CollectionUtils.isEmpty(holdings)) {
            return Map.of();
        }
        return mapNoneStock(holdings);
    }

    <H extends Holding> Map<Holding, List<CommonHoldingsDTO>> mapNoneStock(Map<H, RCommonHoldings> holdings) {
        return holdings.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().getHoldings()));
    }

    <H extends Holding> void validate(Map<H, RCommonHoldings> holdings, List<Warning> warnings, Notification notification) {
        holdings.forEach((key, value) -> {
            if (isNull(value.getHoldings())) {
                notification.addError(ExceptionCode.ERR_TCH_MUH_002.errorWithId(key.generateUserIdentifier()));
            }
        });
        checkWarnings(holdings, warnings);
    }

    <H extends Holding> void checkWarnings(Map<H, RCommonHoldings> holdings, List<Warning> warnings) {
        if (isWarningPresent(holdings)) {
            warnings.add(
                    new Warning(
                            holdings.keySet().stream().findFirst().orElseThrow().generateUserIdentifier(),
                            ExceptionCode.WRN_TCH_MUH_001.getMessage(),
                            ExceptionCode.WRN_TCH_MUH_001.name()
                    ));
        }
    }

    <H extends Holding> boolean isWarningPresent(Map<H, RCommonHoldings> holdings) {
        return holdings.values().stream().filter(e -> e.getHoldings() != null).anyMatch(e -> e.getHoldings().stream()
                .anyMatch(firstLvlChild -> firstLvlRecursionTypes.contains(firstLvlChild.getType())
                        && isNull(firstLvlChild.getUnderlyingHoldings())));
    }

}
