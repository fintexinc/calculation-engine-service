package com.fintex.ce.service.impl.cache;

import com.fintex.smclient.graphql.PaymentFrequencyType;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.GicHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RIncomeForecast;
import com.fintex.ce.repository.graphql.query.IncomeForecastSMRepository;
import com.fintex.ce.repository.redis.IncomeForecastRepository;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_FI_DY_001;
import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_FI_ID_001;
import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_FI_MD_001;
import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_FI_PF_001;
import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_FI_SC_001;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.INCOME_FORECAST;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.FIXED_INCOME_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;
import static java.util.stream.Collectors.toMap;

@Service
public class IncomeForecastCacheStorage extends MultipleCacheStorageAbstract<RIncomeForecast, RIncomeForecast, RIncomeForecast, RIncomeForecast> {

    public IncomeForecastCacheStorage(final IncomeForecastSMRepository fdsRepo,
                                      final IncomeForecastRepository fundCanadaCacheRepo,
                                      final IncomeForecastRepository etfCanadaCacheRepo,
                                      final IncomeForecastRepository etfUsCacheRepo,
                                      final IncomeForecastRepository stockCacheRepo,
                                      final CacheStatisticService cacheStatisticService) {
        super(
                fdsRepo, fundCanadaCacheRepo, etfCanadaCacheRepo, etfUsCacheRepo,
                stockCacheRepo, cacheStatisticService, INCOME_FORECAST
        );
    }

    @Override
    public Map<Holding, RIncomeForecast> load(final List<Holding> holdings, final List<DataProvider> providers,
                                              final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
        Map<Holding, RIncomeForecast> map = new HashMap<>();
        map.putAll(verify(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()), warnings));
        map.putAll(verify(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()), warnings));
        map.putAll(verify(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()), warnings));
        map.putAll(verify(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()), warnings));
        map.putAll(verify(loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of()), warnings));
        map.putAll(verify(loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of()), warnings));
        map.putAll(verify(loadBenchOfFixedIncomes(filterHoldings(holdings, FIXED_INCOME_PREDICATE), List.of()), warnings));
        map.putAll(verify(loadForBenchOfStock(filterHoldings(holdings, STOCK_PREDICATE), List.of()), warnings));
        map.putAll(addGicHoldings(filterHoldings(holdings, GIC_PREDICATE)));
        return map;
    }

    private Map<Holding, RIncomeForecast> addGicHoldings(final List<Holding> holdings) {
        return holdings.stream()
                .map(GicHolding.class::cast)
                .collect(toMap(Function.identity(), this::getRIncomeForecast));
    }

    private RIncomeForecast getRIncomeForecast(final GicHolding gicHolding) {
        final RIncomeForecast rIncomeForecast = new RIncomeForecast();
        rIncomeForecast.setDividendYield(gicHolding.getClientIntRate());
        rIncomeForecast.setHoldingId(gicHolding.getName());
        rIncomeForecast.setId(gicHolding.getType().name());
        return rIncomeForecast;
    }

    <H extends Holding> Map<Holding, RIncomeForecast> verify(final Map<H, RIncomeForecast> holdings,
                                                             final List<Warning> warnings) {
        return holdings.entrySet()
                .stream()
                .collect(toMap(
                        Map.Entry::getKey,
                        e -> incomeForecastMapper(e, warnings))
                );
    }

    <H extends Holding> RIncomeForecast incomeForecastMapper(final Map.Entry<H, RIncomeForecast> entry,
                                                             final List<Warning> warnings) {
        final RIncomeForecast rIncomeForecast = entry.getValue();
        final H holding = entry.getKey();

        if (Objects.isNull(rIncomeForecast.getDividendYield())) {
            warnings.add(WRN_FI_DY_001.warning(holding));
            return rIncomeForecast;
        }

        if (Objects.equals(holding.getType(), HoldingType.FIXED_INCOME)) {
            if (Objects.isNull(rIncomeForecast.getPaymentFrequencyType())) {
                warnings.add(WRN_FI_PF_001.warning(holding));
            }
            if (Objects.equals(rIncomeForecast.getPaymentFrequencyType(), PaymentFrequencyType.AT_MATURITY) &&
                    Objects.isNull(rIncomeForecast.getMaturityDate())) {
                warnings.add(WRN_FI_MD_001.warning(holding));
            }
            if (Objects.equals(rIncomeForecast.getPaymentFrequencyType(), PaymentFrequencyType.AT_MATURITY) &&
                    Objects.isNull(rIncomeForecast.getIssueDate())) {
                warnings.add(WRN_FI_ID_001.warning(holding));
            }
        }

        if (Objects.isNull(rIncomeForecast.getSchedule()) &&
                !isFixedIncomeAtMaturityType(holding, rIncomeForecast) &&
                CollectionUtils.isEmpty(rIncomeForecast.getSchedule())) {
            warnings.add(WRN_FI_SC_001.warning(holding));
        }

        return rIncomeForecast;
    }

    private <H extends Holding> boolean isFixedIncomeAtMaturityType(final H holding,
                                                                    final RIncomeForecast rIncomeForecast) {
        return Objects.equals(holding.getType(), HoldingType.FIXED_INCOME) &&
                Objects.equals(rIncomeForecast.getPaymentFrequencyType(), PaymentFrequencyType.AT_MATURITY);
    }

}
