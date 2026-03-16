package com.fintex.ce.adapter.cache;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.adapter.cache.entity.RYield;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.adapter.cache.repository.YieldRepository;
import com.fintex.ce.adapter.cache.core.CacheStorageAbstract;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_YI_001;
import static com.fintex.ce.constant.CacheNameEntity.YIELD;
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
public class YieldCacheStorage extends CacheStorageAbstract<Yield, RYield, Map<Holding, Yield>> {

  public YieldCacheStorage(final SecurityDataPort<Yield> securityDataPort,
      final CacheEntityMapper<Yield, RYield> yieldMapper,
      final YieldRepository yieldRepository) {
    super(securityDataPort, yieldMapper, yieldRepository, YIELD);
  }

  @Override
  public Map<Holding, Yield> load(final List<? extends Holding> holdings, final List<DataProvider> providers,
      final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
    final Map<Holding, Yield> map = new HashMap<>();
    map.putAll(verify(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()), warnings));
    map.putAll(verify(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()), warnings));
    map.putAll(verify(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()), warnings));
    map.putAll(verify(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()), warnings));
    map.putAll(verify(loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of()),
        warnings));
    map.putAll(verify(loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of()),
        warnings));
    map.putAll(verify(loadForBenchOfStock(filterHoldings(holdings, STOCK_PREDICATE), List.of()), warnings));
    map.putAll(verify(loadBenchOfFixedIncomes(filterHoldings(holdings, FIXED_INCOME_PREDICATE), List.of()), warnings));
    map.putAll(verify(loadBenchOfSeparatelyManagedAccounts(filterHoldings(holdings,
        SEPARATELY_MANAGED_ACCOUNT_PREDICATE), List.of()), warnings));
    map.putAll(addGics(filterHoldings(holdings, GIC_PREDICATE)));
    return map;
  }

  private Map<Holding, Yield> addGics(final List<Holding> holdings) {
    return holdings.stream()
        .map(holding -> (GicHolding) holding)
        .collect(Collectors.toMap(
            Function.identity(),
            this::getYield));
  }

  private Yield getYield(final GicHolding gic) {
    final Yield yield = new Yield();
    yield.setDividendYield(gic.getClientIntRate());
    yield.setHoldingId(gic.getName());
    return yield;
  }

  public <H extends Holding> Map<Holding, Yield> verify(final Map<H, Yield> holdings,
      final List<Warning> warnings) {
    return holdings.entrySet()
        .stream()
        .collect(
            toMap(
                Map.Entry::getKey,
                e -> yieldVerifier(e, warnings)));
  }

  private <H extends Holding> Yield yieldVerifier(final Map.Entry<H, Yield> entry,
      final List<Warning> warnings) {
    return Optional.ofNullable(entry.getValue())
        .filter(v -> Objects.nonNull(v.getDividendYield()))
        .orElseGet(() -> {
          warnings.add(WRN_YI_001.warning(entry.getKey()));
          return new Yield();
        });
  }

}
