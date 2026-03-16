package com.fintex.ce.adapter.cache;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.ExceptionCode;
import com.fintex.ce.constant.CacheNameEntity;
import com.fintex.ce.domain.model.CommonHoldings;
import com.fintex.ce.domain.model.CommonHoldingsDTO;
import com.fintex.ce.domain.model.CommonHoldingsStock;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.exception.GeneralRuntimeException;
import com.fintex.ce.domain.exception.notification.pattern.Notification;
import com.fintex.ce.adapter.cache.entity.topcommonholdings.RCommonHoldings;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.adapter.cache.repository.commonholdings.CommonHoldingsRepository;
import com.fintex.ce.adapter.cache.core.CacheStorageAbstract;
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

import static com.fintex.ce.domain.enumeration.ExceptionCode.ERR_TCH_MUH_002;
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
    extends CacheStorageAbstract<CommonHoldings, RCommonHoldings, Map<Holding, List<CommonHoldingsDTO>>> {

  public Set<String> firstLvlRecursionTypes;
  private static final String ABSENT_STOCK_HOLDING = "There is no corresponding stock holding for that holding:";
  private static final String CASH_GIC_ACCUMULATE_TYPE = "B";
  private static final String CASH_NAME = "Cash";
  private static final String EQUITY_TYPE = "E";

  public CommonHoldingsCacheStorage(
      @Value("#{'${default.top-common-holdings.recursion-types}'.split(',')}") Set<String> firstLvlRecursionTypes,
      SecurityDataPort<CommonHoldings> securityDataPort,
      CacheEntityMapper<CommonHoldings, RCommonHoldings> mapper,
      CommonHoldingsRepository commonHoldingsRepository) {
    super(securityDataPort, mapper, commonHoldingsRepository, CacheNameEntity.TOP_COMMON_HOLDINGS);
    this.firstLvlRecursionTypes = firstLvlRecursionTypes;
  }

  @Override
  public Map<Holding, List<CommonHoldingsDTO>> load(List<? extends Holding> holdings, List<DataProvider> providers,
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
    @SuppressWarnings("unchecked")
    Map<Holding, CommonHoldingsStock> stockData = (Map<Holding, CommonHoldingsStock>) (Map<?, ?>)
        loadForBenchOfStock(filterHoldings(holdings, STOCK_PREDICATE), List.of());
    result.putAll(mapForStock(stockData, paramHolderDTO));
    result.putAll(mapForCash(holdings));
    result.putAll(mapForGic(holdings));
    return result;
  }

  Map<Holding, List<CommonHoldingsDTO>> mapForCash(final List<? extends Holding> holdings) {
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

  Map<Holding, List<CommonHoldingsDTO>> mapForGic(final List<? extends Holding> holdings) {
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

  public <H extends Holding> Map<Holding, List<CommonHoldingsDTO>> mapForStock(
      Map<H, CommonHoldingsStock> stockHoldings,
      ParamHolderDTO paramHolderDTO) {
    return stockHoldings.entrySet().stream().collect(toMap(
        Map.Entry::getKey,
        e -> List.of(initializeStockCommonHoldingsDTO(e, paramHolderDTO))));
  }

  public <H extends Holding> CommonHoldingsDTO initializeStockCommonHoldingsDTO(
      Map.Entry<H, CommonHoldingsStock> stockHolding,
      ParamHolderDTO paramHolderDTO) {
    var stock = stockHolding.getValue();
    return new CommonHoldingsDTO(
        stock.getCompanyName(),
        EQUITY_TYPE,
        calculateStockHoldingValue(stockHolding, paramHolderDTO),
        stock.getTicker(),
        stock.getExchangeCode());
  }

  public <H extends Holding> BigDecimal calculateStockHoldingValue(Map.Entry<H, CommonHoldingsStock> stockHolding,
      ParamHolderDTO paramHolderDTO) {
    return paramHolderDTO.getAllocations().entrySet().stream()
        .filter(e -> stockHolding.getKey().generateUserIdentifier().equals(e.getKey().generateUserIdentifier()))
        .findFirst()
        .orElseThrow(() -> new GeneralRuntimeException(ABSENT_STOCK_HOLDING + stockHolding))
        .getValue();
  }

  public <H extends Holding> Map<Holding, List<CommonHoldingsDTO>> mapForNoneStock(Map<H, CommonHoldings> holdings) {
    if (CollectionUtils.isEmpty(holdings)) {
      return Map.of();
    }
    return mapNoneStock(holdings);
  }

  public <H extends Holding> Map<Holding, List<CommonHoldingsDTO>> mapNoneStock(Map<H, CommonHoldings> holdings) {
    return holdings.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> mapToCommonHoldingsDTO(e.getValue()
        .getHoldings())));
  }

  private List<CommonHoldingsDTO> mapToCommonHoldingsDTO(List<CommonHoldings.CommonHolding> holdings) {
    if (holdings == null) {
      return null;
    }
    return holdings.stream()
        .map(h -> {
          var dto = new CommonHoldingsDTO();
          dto.setUuid(h.getUuid());
          dto.setName(h.getName());
          dto.setType(h.getType());
          dto.setValue(h.getValue());
          dto.setTicker(h.getTicker());
          dto.setExchangeCode(h.getExchangeCode());
          dto.setUnderlyingHoldings(mapToCommonHoldingsDTO(h.getUnderlyingHoldings()));
          return dto;
        })
        .toList();
  }

  public <H extends Holding> void validate(Map<H, CommonHoldings> holdings, List<Warning> warnings,
      Notification notification) {
    holdings.forEach((key, value) -> {
      if (isNull(value.getHoldings())) {
        notification.addError(ERR_TCH_MUH_002.errorWithId(key.generateUserIdentifier()));
      }
    });
    checkWarnings(holdings, warnings);
  }

  public <H extends Holding> void checkWarnings(Map<H, CommonHoldings> holdings, List<Warning> warnings) {
    if (isWarningPresent(holdings)) {
      warnings.add(
          new Warning(
              holdings.keySet().stream().findFirst().orElseThrow().generateUserIdentifier(),
              ExceptionCode.WRN_TCH_MUH_001.getMessage(),
              ExceptionCode.WRN_TCH_MUH_001.name()));
    }
  }

  public <H extends Holding> boolean isWarningPresent(Map<H, CommonHoldings> holdings) {
    return holdings.values().stream().filter(e -> e.getHoldings() != null).anyMatch(e -> e.getHoldings().stream()
        .anyMatch(firstLvlChild -> firstLvlRecursionTypes.contains(firstLvlChild.getType())
            && isNull(firstLvlChild.getUnderlyingHoldings())));
  }

}
