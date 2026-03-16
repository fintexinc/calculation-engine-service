package com.fintex.ce.adapter.cache;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.calculation.CreditQualityRating;
import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.cache.entity.RCreditQuality;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.adapter.cache.repository.CreditQualityRepository;
import com.fintex.ce.adapter.cache.core.CacheStorageAbstract;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_CQ_CQ_001;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_UNKNOWN_001;
import static com.fintex.ce.constant.CacheNameEntity.CREDIT_QUALITY;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.FIXED_INCOME_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;
import static java.util.stream.Collectors.toMap;

@Service
public class CreditQualityCacheStorage
    extends CacheStorageAbstract<CreditQuality, RCreditQuality, Map<Holding, Map<CreditQualityRating, BigDecimal>>> {

  public CreditQualityCacheStorage(
      SecurityDataPort<CreditQuality> securityDataPort,
      CacheEntityMapper<CreditQuality, RCreditQuality> mapper,
      CreditQualityRepository creditQualityRepository) {
    super(securityDataPort, mapper, creditQualityRepository, CREDIT_QUALITY);
  }

  @Override
  public Map<Holding, Map<CreditQualityRating, BigDecimal>> load(final List<? extends Holding> holdings,
      final List<DataProvider> providers,
      final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
    Map<Holding, Map<CreditQualityRating, BigDecimal>> map = new HashMap<>();
    map.putAll(mapToRatings(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()),
        warnings));
    map.putAll(mapToRatings(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()), warnings));
    map.putAll(mapToRatings(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()),
        warnings));
    map.putAll(mapToRatings(loadForBenchOfBenchmarks(filterHoldings(holdings, BENCHMARKS_PREDICATE), List.of()),
        warnings));
    map.putAll(mapToRatings(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()),
        warnings));
    map.putAll(mapToRatings(loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of()),
        warnings));
    map.putAll(mapToRatings(loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of()),
        warnings));
    map.putAll(mapToRatings(loadBenchOfFixedIncomes(filterHoldings(holdings, FIXED_INCOME_PREDICATE), List.of()),
        warnings));
    map.putAll(addGics(filterHoldings(holdings, GIC_PREDICATE)));
    return map;
  }

  public Map<Holding, Map<CreditQualityRating, BigDecimal>> addGics(final List<Holding> holdings) {
    final HashMap<Holding, Map<CreditQualityRating, BigDecimal>> result = new HashMap<>();
    for (final Holding holding : holdings) {
      final GicHolding gic = (GicHolding) holding;
      if (!gic.isLessThanOneYearOld()) {
        result.put(holding, Map.of(CreditQualityRating.AAA, BigDecimal.ONE));
      }
    }
    return result;
  }

  public <H extends Holding> Map<Holding, Map<CreditQualityRating, BigDecimal>> mapToRatings(
      final Map<H, CreditQuality> holdings,
      final List<Warning> warnings) {
    return holdings.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> mapRatings(e.getKey(), e.getValue(),
        warnings)));
  }

  public Map<CreditQualityRating, BigDecimal> mapRatings(final Holding holding, final CreditQuality creditQuality,
      final List<Warning> warnings) {
    if (CollectionUtils.isEmpty(creditQuality.getRatings())) {
      warnings.add(WRN_CQ_CQ_001.warning(holding));
      return Map.of();
    }
    final Map<CreditQualityRating, BigDecimal> map = new EnumMap<>(CreditQualityRating.class);
    creditQuality.getRatings().forEach((ratingStr, value) -> {
      final CreditQualityRating rating = CreditQualityRating.of(ratingStr);
      if (rating == null) {
        warnings.add(WRN_UNKNOWN_001.warning(holding, ratingStr, "Credit Quality"));
      } else {
        map.put(rating, value);
      }
    });
    return map;
  }

}
