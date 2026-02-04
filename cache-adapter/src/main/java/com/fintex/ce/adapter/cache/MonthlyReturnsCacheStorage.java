package com.fintex.ce.adapter.cache;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.MonthlyReturns;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.port.output.ReturnsGenerator;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.cache.entity.RMonthlyReturns;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.graphql.MultipleSMRepository;
import com.fintex.ce.adapter.cache.repository.monthlyreturns.MonthlyReturnsRepository;
import com.fintex.ce.adapter.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static com.fintex.ce.domain.enumeration.Currency.of;
import static com.fintex.ce.constant.CacheNameEntity.MONTHLY_RETURNS;
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
public class MonthlyReturnsCacheStorage
    extends
      MultipleCacheStorageAbstract<MonthlyReturns, MonthlyReturns, MonthlyReturns, MonthlyReturns, RMonthlyReturns> {

  private final TBillsCacheStorage tBillsCacheStorage;
  private final ReturnsGenerator monthlyReturnGenerator;

  public MonthlyReturnsCacheStorage(
      final MultipleSMRepository<MonthlyReturns, MonthlyReturns, MonthlyReturns, MonthlyReturns> smRepo,
      final CacheEntityMapper<MonthlyReturns, RMonthlyReturns> mapper,
      final MonthlyReturnsRepository monthlyReturnsRepository,
      final TBillsCacheStorage tBillsCacheStorage,
      final CacheStatisticService cacheStatisticService,
      final ReturnsGenerator monthlyReturnGenerator) {
    super(
        smRepo, mapper, mapper, mapper, mapper,
        monthlyReturnsRepository, monthlyReturnsRepository,
        monthlyReturnsRepository, monthlyReturnsRepository, cacheStatisticService, MONTHLY_RETURNS);
    this.tBillsCacheStorage = tBillsCacheStorage;
    this.monthlyReturnGenerator = monthlyReturnGenerator;
  }

  @Override
  public Map<Holding, MonthlyReturns> load(final List<Holding> holdings, final List<DataProvider> providers,
      final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
    final Map<Holding, MonthlyReturns> map = new HashMap<>();
    map.putAll(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()));
    map.putAll(loadForBenchOfStock(filterHoldings(holdings, STOCK_PREDICATE), List.of()));
    map.putAll(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()));
    map.putAll(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()));
    map.putAll(loadForBenchOfBenchmarks(filterHoldings(holdings, BENCHMARKS_PREDICATE), List.of()));
    map.putAll(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()));
    map.putAll(loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of()));
    map.putAll(loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of()));
    map.putAll(loadBenchOfFixedIncomes(filterHoldings(holdings, FIXED_INCOME_PREDICATE), List.of()));
    map.putAll(loadBenchOfSeparatelyManagedAccounts(filterHoldings(holdings, SEPARATELY_MANAGED_ACCOUNT_PREDICATE), List
        .of()));
    map.putAll(loadBenchOfPagGuidedPortfolios(filterHoldings(holdings, PAG_GUIDED_PORTFOLIO_PREDICATE), List.of()));
    // we have this condition to avoid NPE while warming the caches
    // otherwise Engineer should check for currency to not be null on caller method
    if (paramHolderDTO != null) {
      addCashReturns(holdings, paramHolderDTO.getCurrency(), map);
    }
    return map;
  }

  public void addCashReturns(final List<Holding> holdings, final Currency currencyFromRequest,
      final Map<Holding, MonthlyReturns> map) {
    final List<CashHolding> cashes = filterHoldings(holdings, CASH_PREDICATE);
    if (!cashes.isEmpty()) {
      final Map<CashHolding, MonthlyReturns> cashHoldingMonthlyReturnsMap = cashes
          .stream()
          .collect(toMap(cashHolding -> cashHolding,
              cashHolding -> getMonthlyReturns(currencyFromRequest, cashHolding)));
      map.putAll(cashHoldingMonthlyReturnsMap);
    }
  }

  private MonthlyReturns getMonthlyReturns(final Currency currencyFromRequest, final CashHolding cashHolding) {
    MonthlyReturns monthlyReturns = new MonthlyReturns();
    monthlyReturns.setCurrency(checkCurrency(cashHolding.getCurrency(), currencyFromRequest));
    monthlyReturns.setHoldingType(convertHoldingType(cashHolding.getType()));
    monthlyReturns.setReturns(getReturns(currencyFromRequest, cashHolding));
    return monthlyReturns;
  }

  private HoldingType convertHoldingType(final com.fintex.ce.domain.enumeration.HoldingType apiType) {
    if (apiType == null) {
      return null;
    }
    return HoldingType.valueOf(apiType.name());
  }

  private TreeMap<LocalDate, BigDecimal> getReturns(Currency currencyFromRequest, CashHolding cashHolding) {
    if (cashHolding.hasClientIntRate()) {
      return monthlyReturnGenerator.generateReturns(cashHolding);
    } else {
      return tBillsCacheStorage.loadTBillsFor(of(checkCurrency(cashHolding.getCurrency(), currencyFromRequest)));
    }
  }

  public String checkCurrency(final Currency cashCurrency, final Currency currencyFromRequest) {
    return Objects.isNull(cashCurrency) ? currencyFromRequest.name() : cashCurrency.name();
  }

}
