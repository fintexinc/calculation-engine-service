package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.domain.monthlyreturns.MonthlyReturnsGenerator;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.CashHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.repository.graphql.query.MonthlyReturnsSMRepository;
import com.fintex.ce.repository.redis.monthlyreturns.MonthlyReturnsRepository;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static com.fintex.ce.config.enumeration.Currency.of;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.FIXED_INCOME_PREDICATE;
import static com.fintex.ce.util.FilterUtils.PAG_GUIDED_PORTFOLIO_PREDICATE;
import static com.fintex.ce.util.FilterUtils.SEPARATELY_MANAGED_ACCOUNT_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;

@Service
public class MonthlyReturnsCacheStorage extends MultipleCacheStorageAbstract<RMonthlyReturns, RMonthlyReturns, RMonthlyReturns, RMonthlyReturns> {

    private final TBillsCacheStorage tBillsCacheStorage;
    private final MonthlyReturnsGenerator monthlyReturnGenerator;

    @Autowired
    public MonthlyReturnsCacheStorage(final MonthlyReturnsSMRepository queryRepository,
                                      final MonthlyReturnsRepository monthlyReturnsRepository,
                                      final TBillsCacheStorage tBillsCacheStorage,
                                      final CacheStatisticService cacheStatisticService,
                                      final MonthlyReturnsGenerator monthlyReturnGenerator) {
        super(
                queryRepository, monthlyReturnsRepository, monthlyReturnsRepository,
                monthlyReturnsRepository, monthlyReturnsRepository, cacheStatisticService, MONTHLY_RETURNS
        );
        this.tBillsCacheStorage = tBillsCacheStorage;
        this.monthlyReturnGenerator = monthlyReturnGenerator;
    }

    @Override
    public Map<Holding, RMonthlyReturns> load(final List<Holding> holdings, final List<DataProvider> providers,
                                              final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
        final Map<Holding, RMonthlyReturns> map = new HashMap<>();
        map.putAll(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()));
        map.putAll(loadForBenchOfStock(filterHoldings(holdings, STOCK_PREDICATE), List.of()));
        map.putAll(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()));
        map.putAll(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()));
        map.putAll(loadForBenchOfBenchmarks(filterHoldings(holdings, BENCHMARKS_PREDICATE), List.of()));
        map.putAll(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()));
        map.putAll(loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of()));
        map.putAll(loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of()));
        map.putAll(loadBenchOfFixedIncomes(filterHoldings(holdings, FIXED_INCOME_PREDICATE), List.of()));
        map.putAll(loadBenchOfSeparatelyManagedAccounts(filterHoldings(holdings, SEPARATELY_MANAGED_ACCOUNT_PREDICATE), List.of()));
        map.putAll(loadBenchOfPagGuidedPortfolios(filterHoldings(holdings, PAG_GUIDED_PORTFOLIO_PREDICATE), List.of()));
        // we have this condition to avoid NPE while warming the caches
        // otherwise Engineer should check for currency to not be null on caller method
        if (paramHolderDTO != null) {
            addCashReturns(holdings, paramHolderDTO.getCurrency(), map);
        }
        return map;
    }

    void addCashReturns(final List<Holding> holdings, final Currency currencyFromRequest, final Map<Holding, RMonthlyReturns> map) {
        final List<CashHolding> cashes = filterHoldings(holdings, CASH_PREDICATE);
        if (!cashes.isEmpty()) {
            final Map<CashHolding, RMonthlyReturns> cashHoldingRMonthlyReturnsMap = cashes
                    .stream()
                    .collect(toMap(cashHolding -> cashHolding,
                            cashHolding -> getMonthlyReturns(currencyFromRequest, cashHolding)));
            map.putAll(cashHoldingRMonthlyReturnsMap);
        }
    }

    private RMonthlyReturns getMonthlyReturns(final Currency currencyFromRequest, final CashHolding cashHolding) {
        return new RMonthlyReturns(
                checkCurrency(cashHolding.getCurrency(), currencyFromRequest),
                cashHolding.getType(),
                getReturns(currencyFromRequest, cashHolding));
    }

    private TreeMap<LocalDate, BigDecimal> getReturns(Currency currencyFromRequest, CashHolding cashHolding) {
        if (cashHolding.hasClientIntRate()) {
            return monthlyReturnGenerator.generateReturns(cashHolding);
        } else {
            return tBillsCacheStorage.loadTBillsFor(of(checkCurrency(cashHolding.getCurrency(), currencyFromRequest)));
        }
    }

    String checkCurrency(final Currency cashCurrency, final Currency currencyFromRequest) {
        return Objects.isNull(cashCurrency) ? currencyFromRequest.name() : cashCurrency.name();
    }

}
