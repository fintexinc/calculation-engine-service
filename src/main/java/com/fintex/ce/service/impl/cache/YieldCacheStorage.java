package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.GicHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RYield;
import com.fintex.ce.repository.graphql.query.YieldSMRepository;
import com.fintex.ce.repository.redis.YieldRepository;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_YI_001;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.YIELD;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.FIXED_INCOME_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.SEPARATELY_MANAGED_ACCOUNT_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;
import static java.util.stream.Collectors.toMap;

@Service
public class YieldCacheStorage extends MultipleCacheStorageAbstract<RYield, RYield, RYield, RYield> {

	public YieldCacheStorage(final YieldSMRepository fdsRepo,
							 final YieldRepository fundCanadaCacheRepo,
							 final YieldRepository etfCanadaCacheRepo,
							 final YieldRepository etfUsCacheRepo,
							 final YieldRepository stockCacheRepo,
							 final CacheStatisticService cacheStatisticService) {
		super(
				fdsRepo, fundCanadaCacheRepo, etfCanadaCacheRepo, etfUsCacheRepo,
				stockCacheRepo, cacheStatisticService, YIELD
		);
	}

	@Override
	public Map<Holding, RYield> load(final List<Holding> holdings, final List<DataProvider> providers,
									 final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
		final Map<Holding, RYield> map = new HashMap<>();
		map.putAll(verify(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()), warnings));
		map.putAll(verify(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()), warnings));
		map.putAll(verify(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()), warnings));
		map.putAll(verify(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()), warnings));
		map.putAll(verify(loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of()), warnings));
		map.putAll(verify(loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of()), warnings));
		map.putAll(verify(loadForBenchOfStock(filterHoldings(holdings, STOCK_PREDICATE), List.of()), warnings));
		map.putAll(verify(loadBenchOfFixedIncomes(filterHoldings(holdings, FIXED_INCOME_PREDICATE), List.of()), warnings));
		map.putAll(verify(loadBenchOfSeparatelyManagedAccounts(filterHoldings(holdings, SEPARATELY_MANAGED_ACCOUNT_PREDICATE), List.of()), warnings));
		map.putAll(addGics(filterHoldings(holdings, GIC_PREDICATE)));
		return map;
	}

	private Map<Holding, RYield> addGics(final List<Holding> holdings) {
        return holdings.stream()
				.map(holding -> (GicHolding) holding)
                .collect(Collectors.toMap(
						Function.identity(),
						this::getRYield)
				);
	}

    private RYield getRYield(final GicHolding gic) {
        final RYield rYield = new RYield();
        rYield.setDividendYield(gic.getClientIntRate());
        rYield.setHoldingId(gic.getName());
        rYield.setId(gic.getType().toString());
		return rYield;
    }

    protected <H extends Holding> Map<Holding, RYield> verify(final Map<H, RYield> holdings,
															  final List<Warning> warnings) {
		return holdings.entrySet()
				.stream()
				.collect(
						toMap(
								Map.Entry::getKey,
								e -> yieldMapper(e, warnings)
						)
				);
	}

	private <H extends Holding> RYield yieldMapper(final Map.Entry<H, RYield> entry,
												   final List<Warning> warnings) {
		return Optional.ofNullable(entry.getValue())
				.filter(v -> Objects.nonNull(v.getDividendYield()))
				.orElseGet(() -> {
					warnings.add(WRN_YI_001.warning(entry.getKey()));
					return new RYield();
				});
	}

}
