package com.fintex.ce.adapter.cache;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.IncomeForecast;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.cache.entity.RIncomeForecast;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.adapter.cache.repository.IncomeForecastRepository;
import com.fintex.ce.adapter.cache.core.CacheStorageAbstract;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_FI_DY_001;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_FI_ID_001;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_FI_MD_001;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_FI_PF_001;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_FI_SC_001;
import static com.fintex.ce.constant.CacheNameEntity.INCOME_FORECAST;
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
public class IncomeForecastCacheStorage
    extends CacheStorageAbstract<IncomeForecast, RIncomeForecast, Map<Holding, IncomeForecast>> {

  public IncomeForecastCacheStorage(
      final SecurityDataPort<IncomeForecast> securityDataPort,
      final CacheEntityMapper<IncomeForecast, RIncomeForecast> mapper,
      final IncomeForecastRepository incomeForecastRepository) {
    super(securityDataPort, mapper, incomeForecastRepository, INCOME_FORECAST);
  }

  @Override
  public Map<Holding, IncomeForecast> load(final List<? extends Holding> holdings, final List<DataProvider> providers,
      final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
    Map<Holding, IncomeForecast> map = new HashMap<>();
    map.putAll(verify(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()), warnings));
    map.putAll(verify(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()), warnings));
    map.putAll(verify(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()), warnings));
    map.putAll(verify(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()), warnings));
    map.putAll(verify(loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of()),
        warnings));
    map.putAll(verify(loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of()),
        warnings));
    map.putAll(verify(loadBenchOfFixedIncomes(filterHoldings(holdings, FIXED_INCOME_PREDICATE), List.of()), warnings));
    map.putAll(verify(loadForBenchOfStock(filterHoldings(holdings, STOCK_PREDICATE), List.of()), warnings));
    map.putAll(addGicHoldings(filterHoldings(holdings, GIC_PREDICATE)));
    return map;
  }

  private Map<Holding, IncomeForecast> addGicHoldings(final List<Holding> holdings) {
    return holdings.stream()
        .map(GicHolding.class::cast)
        .collect(toMap(Function.identity(), this::getIncomeForecast));
  }

  private IncomeForecast getIncomeForecast(final GicHolding gicHolding) {
    final IncomeForecast incomeForecast = new IncomeForecast();
    incomeForecast.setDividendYield(gicHolding.getClientIntRate());
    incomeForecast.setHoldingId(gicHolding.getName());
    return incomeForecast;
  }

  public <H extends Holding> Map<Holding, IncomeForecast> verify(final Map<H, IncomeForecast> holdings,
      final List<Warning> warnings) {
    return holdings.entrySet()
        .stream()
        .collect(toMap(
            Map.Entry::getKey,
            e -> incomeForecastMapper(e, warnings)));
  }

  public <H extends Holding> IncomeForecast incomeForecastMapper(final Map.Entry<H, IncomeForecast> entry,
      final List<Warning> warnings) {
    final IncomeForecast incomeForecast = entry.getValue();
    final H holding = entry.getKey();

    if (Objects.isNull(incomeForecast.getDividendYield())) {
      warnings.add(WRN_FI_DY_001.warning(holding));
      return incomeForecast;
    }

    if (Objects.equals(holding.getType(), HoldingType.FIXED_INCOME)) {
      if (Objects.isNull(incomeForecast.getPaymentFrequencyType())) {
        warnings.add(WRN_FI_PF_001.warning(holding));
      }
      if (Objects.equals(incomeForecast.getPaymentFrequencyType(), "AT_MATURITY") &&
          Objects.isNull(incomeForecast.getMaturityDate())) {
        warnings.add(WRN_FI_MD_001.warning(holding));
      }
      if (Objects.equals(incomeForecast.getPaymentFrequencyType(), "AT_MATURITY") &&
          Objects.isNull(incomeForecast.getIssueDate())) {
        warnings.add(WRN_FI_ID_001.warning(holding));
      }
    }

    if (Objects.isNull(incomeForecast.getSchedule()) &&
        !isFixedIncomeAtMaturityType(holding, incomeForecast) &&
        CollectionUtils.isEmpty(incomeForecast.getSchedule())) {
      warnings.add(WRN_FI_SC_001.warning(holding));
    }

    return incomeForecast;
  }

  private <H extends Holding> boolean isFixedIncomeAtMaturityType(final H holding,
      final IncomeForecast incomeForecast) {
    return Objects.equals(holding.getType(), HoldingType.FIXED_INCOME) &&
        Objects.equals(incomeForecast.getPaymentFrequencyType(), "AT_MATURITY");
  }

}
