package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.FixedIncomeBondSectorCalculation;
import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.application.mapping.AssetAllocationDataMapper;
import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeBondSecurities;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.FixedIncomeSectorResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.error.Warning;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.AllocationMappingUtils;
import com.fintex.ce.util.ExposureDataHolder;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSecuritiesAllocationType;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;

import static com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion.CASH;
import static com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion.FIXED_INCOME;
import static com.fintex.ce.model.error.ErrorCode.MISSING_FIXED_INCOME_BOND_SECTOR;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;
import static java.math.BigDecimal.ZERO;

@Service
@RequiredArgsConstructor
public class FixedIncomeBondSectorCalculationServiceImpl
    extends
      BreakdownAbstractService<FixedIncomeSectorResult, FixedIncomeSecuritiesAllocationType> {

  public static final Map<FixedIncomeSecuritiesAllocationType, BigDecimal> DEFAULT_MAP = new HashMap<>();

  static final Map<FixedIncomeSecuritiesAllocationType, BigDecimal> ALLOCATION_DEFAULT_MAP;

  static {
    Stream.of(FixedIncomeSecuritiesAllocationType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
    ALLOCATION_DEFAULT_MAP = Collections.unmodifiableMap(
        Stream.of(FixedIncomeSecuritiesAllocationType.values()).collect(java.util.stream.Collectors.toMap(type -> type,
            type -> ZERO)));
  }

  private final SecurityDataFetcher<FixedIncomeBondSecurities> fixedIncomeBondSectorSecurityDataFetcher;
  private final SecurityDataFetcher<HoldingAssetAllocation> assetAllocationSecurityDataFetcher;
  private final AssetAllocationDataMapper assetAllocationDataMapper;
  private final DefaultDataProperties defaultDataProperties;

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.FIXED_INCOME_BOND_SECTOR;
  }

  @Override
  public ExposureDataHolder<FixedIncomeSecuritiesAllocationType> fetchExposures(PortfolioHoldingsCommand reqDTO) {
    Map<PortfolioHolding, FixedIncomeBondSecurities> rawData = fixedIncomeBondSectorSecurityDataFetcher.fetch(
        reqDTO.getHoldings(), List.of());
    return AllocationMappingUtils.mapTypedAllocations(rawData,
        FixedIncomeBondSecurities::getFixedIncomeBondSectors,
        ALLOCATION_DEFAULT_MAP, MISSING_FIXED_INCOME_BOND_SECTOR);
  }

  @Override
  public FixedIncomeSectorResult perform(PortfolioHoldingsCommand command) {
    ExposureDataHolder<FixedIncomeSecuritiesAllocationType> exposureData = fetchExposures(command);
    List<DataProvider> providers = getSpecifiedIfEmpty(command.getDataProviders(),
        defaultDataProperties.getDataProviders());
    return calculate(exposureData, command.getHoldings(), providers);
  }

  @Override
  public FixedIncomeSectorResult calculate(ExposureDataHolder<FixedIncomeSecuritiesAllocationType> exposureData,
      List<PortfolioHolding> holdings) {
    return calculate(exposureData, holdings, defaultDataProperties.getDataProviders());
  }

  private FixedIncomeSectorResult calculate(ExposureDataHolder<FixedIncomeSecuritiesAllocationType> exposureData,
      List<PortfolioHolding> holdings, List<DataProvider> dataProviders) {
    var exposures = exposureData.allocations();
    var warnings = new ArrayList<>(exposureData.warnings());
    if (PortfolioUtils.areAllValuesZerosInMap(exposures)) {
      FixedIncomeSectorResult defaultResult = new FixedIncomeSectorResult();
      defaultResult.setFixedIncomeSector(DEFAULT_MAP);
      defaultResult.setWarnings(warnings);
      return defaultResult;
    }

    Map<PortfolioHolding, BigDecimal> fixedIncomePlusCash = getFixedIncomePlusCash(holdings, warnings, dataProviders);

    FixedIncomeBondSectorCalculation calculation = new FixedIncomeBondSectorCalculation(exposures, holdings, warnings,
        fixedIncomePlusCash);
    return calculation.calculate();
  }

  private Map<PortfolioHolding, BigDecimal> getFixedIncomePlusCash(final List<PortfolioHolding> holdings,
      final List<Warning> warnings, final List<DataProvider> dataProviders) {
    final Map<PortfolioHolding, HoldingAssetAllocation> rawData = assetAllocationSecurityDataFetcher.fetch(
        holdings, dataProviders);
    var assetAllocations = assetAllocationDataMapper.toRegionExposures(rawData);
    return assetAllocations.entrySet()
        .stream()
        .collect(toMap(Map.Entry::getKey, this::getFixedIncomePlusCashValue));
  }

  private BigDecimal getFixedIncomePlusCashValue(
      Map.Entry<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> entry) {
    BigDecimal fixedIncome = entry.getValue().getOrDefault(FIXED_INCOME, ZERO);
    BigDecimal cash = entry.getValue().getOrDefault(CASH, ZERO);
    return fixedIncome.add(cash);
  }

}
