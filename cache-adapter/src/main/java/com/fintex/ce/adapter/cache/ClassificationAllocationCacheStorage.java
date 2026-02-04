package com.fintex.ce.adapter.cache;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.constant.CacheNameEntity;
import com.fintex.ce.domain.enumeration.calculation.ClassificationAllocationType;
import com.fintex.ce.domain.model.ClassificationAllocation;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.cache.entity.RClassificationAllocation;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.graphql.MultipleSMRepository;
import com.fintex.ce.adapter.cache.repository.ClassificationAllocationRepository;
import com.fintex.ce.adapter.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_CA_CA_001;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_UNKNOWN_001;
import static com.fintex.ce.domain.enumeration.calculation.ClassificationAllocationType.CASH_AND_CASH_EQUIVALENTS__CANADA;
import static com.fintex.ce.domain.enumeration.calculation.ClassificationAllocationType.CASH_AND_CASH_EQUIVALENTS__US;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.FIXED_INCOME_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

@Service
public class ClassificationAllocationCacheStorage
    extends
      MultipleCacheStorageAbstract<ClassificationAllocation, ClassificationAllocation, ClassificationAllocation, ClassificationAllocation, RClassificationAllocation> {
  public static final Map<ClassificationAllocationType, BigDecimal> DEFAULT_MAP;

  static final Map<HoldingType, ClassificationAllocationType> UNCLASSIFIED_MAP;

  static {
    DEFAULT_MAP = Collections.unmodifiableMap(
        Stream.of(ClassificationAllocationType.values()).collect(toMap(type -> type, type -> ZERO)));

    UNCLASSIFIED_MAP = Map.of(
        HoldingType.CANADA_MUTUAL_FUNDS, ClassificationAllocationType.UNCLASSIFIED__CANADA,
        HoldingType.CANADA_ETF, ClassificationAllocationType.UNCLASSIFIED__UNCLASSIFIED,
        HoldingType.US_ETF, ClassificationAllocationType.UNCLASSIFIED__UNCLASSIFIED,
        HoldingType.US_MUTUAL_FUNDS, ClassificationAllocationType.UNCLASSIFIED__US,
        HoldingType.CANADA_STOCKS, ClassificationAllocationType.EQUITY__UNCLASSIFIED,
        HoldingType.US_STOCKS, ClassificationAllocationType.EQUITY__UNCLASSIFIED,
        HoldingType.FIXED_INCOME, ClassificationAllocationType.FIXED_INCOME__UNCLASSIFIED);

  }

  public ClassificationAllocationCacheStorage(
      final MultipleSMRepository<ClassificationAllocation, ClassificationAllocation, ClassificationAllocation, ClassificationAllocation> smRepo,
      final CacheEntityMapper<ClassificationAllocation, RClassificationAllocation> mapper,
      final ClassificationAllocationRepository fundCanadaCacheRepo,
      final ClassificationAllocationRepository etfCanadaCacheRepo,
      final ClassificationAllocationRepository etfUsCacheRepo,
      final ClassificationAllocationRepository stockCacheRepo,
      final CacheStatisticService cacheStatisticService) {
    super(
        smRepo, mapper, mapper, mapper, mapper,
        fundCanadaCacheRepo, etfCanadaCacheRepo, etfUsCacheRepo,
        stockCacheRepo, cacheStatisticService, CacheNameEntity.CLASSIFICATION_ALLOCATION);
  }

  @Override
  public Map<Holding, Map<ClassificationAllocationType, BigDecimal>> load(final List<Holding> holdings,
      final List<DataProvider> providers,
      final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
    Map<Holding, Map<ClassificationAllocationType, BigDecimal>> map = new HashMap<>();
    map.putAll(getCashTypeByCurrency(filterHoldings(holdings, CASH_PREDICATE), CashHolding::getCurrency, Currency.CAD,
        CASH_AND_CASH_EQUIVALENTS__CANADA));
    map.putAll(getCashTypeByCurrency(filterHoldings(holdings, CASH_PREDICATE), CashHolding::getCurrency, Currency.USD,
        CASH_AND_CASH_EQUIVALENTS__US));
    map.putAll(getCashTypeByCurrency(filterHoldings(holdings, GIC_PREDICATE), GicHolding::getCurrency, Currency.CAD,
        CASH_AND_CASH_EQUIVALENTS__CANADA));
    map.putAll(getCashTypeByCurrency(filterHoldings(holdings, GIC_PREDICATE), GicHolding::getCurrency, Currency.USD,
        CASH_AND_CASH_EQUIVALENTS__US));
    map.putAll(mapResponse(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()),
        warnings));
    map.putAll(mapResponse(loadBenchOfFixedIncomes(filterHoldings(holdings, FIXED_INCOME_PREDICATE), List.of()),
        warnings));
    map.putAll(mapResponse(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()),
        warnings));
    map.putAll(mapResponse(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()), warnings));
    map.putAll(mapResponse(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()), warnings));
    map.putAll(mapResponse(loadForBenchOfStock(filterHoldings(holdings, STOCK_PREDICATE), List.of()), warnings));
    return map;
  }

  public <T extends Holding> Map<Holding, Map<ClassificationAllocationType, BigDecimal>> getCashTypeByCurrency(
      final List<T> holdings,
      final Function<T, Currency> getCurrencyFunction,
      final Currency currency,
      final ClassificationAllocationType allocationType) {
    return filterHoldings(holdings, holding -> Objects.equals(getCurrencyFunction.apply(holding), currency))
        .stream()
        .collect(Collectors.toMap(Function.identity(), h -> Map.of(allocationType, BigDecimal.ONE)));
  }

  public <H extends Holding> Map<Holding, Map<ClassificationAllocationType, BigDecimal>> mapResponse(
      final Map<H, ClassificationAllocation> holdings,
      final List<Warning> warnings) {
    return holdings.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> getClassificationAllocationMapper(e, warnings)));
  }

  public <H extends Holding> Map<ClassificationAllocationType, BigDecimal> getClassificationAllocationMapper(
      final Map.Entry<H, ClassificationAllocation> entry,
      final List<Warning> warnings) {
    final Map<ClassificationAllocationType, BigDecimal> map = new EnumMap<>(DEFAULT_MAP);
    final ClassificationAllocation entryValue = entry.getValue();

    if (Objects.isNull(entryValue) || CollectionUtils.isEmpty(entryValue.getSecurityClassificationValues())) {
      Optional.ofNullable(entry.getKey().getType()) // HoldingType
          .map(UNCLASSIFIED_MAP::get)
          .ifPresentOrElse(type -> map.put(type, BigDecimal.ONE),
              () -> warnings.add(WRN_CA_CA_001.warning(entry.getKey())));
      return map;
    }

    entryValue.getSecurityClassificationValues()
        .forEach((typeStr, value) -> {
          final ClassificationAllocationType type = ClassificationAllocationType.of(typeStr);
          Optional.ofNullable(type)
              .ifPresentOrElse(
                  classificationAllocationType -> map.put(classificationAllocationType, value),
                  () -> warnings.add(
                      WRN_UNKNOWN_001.warning(entry.getKey(), typeStr, "FDS Get Calculation Allocation")));
        });

    return map;
  }

}
