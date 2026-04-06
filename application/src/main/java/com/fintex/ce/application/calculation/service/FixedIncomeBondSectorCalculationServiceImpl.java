package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.FixedIncomeBondSectorCalculation;
import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.application.mapping.AssetAllocationDataMapper;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.ce.domain.model.HoldingAssetAllocation;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegion;
import com.fintex.sm.model.domain.enumeration.FixedIncomeSecuritiesAllocationType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.FixedIncomeSectorResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.AllocationMappingUtils;
import com.fintex.ce.util.ExposureDataHolder;
import com.fintex.ce.util.PortfolioUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import static com.fintex.ce.domain.model.calculation.AssetAllocationRegion.CASH;
import static com.fintex.ce.domain.model.calculation.AssetAllocationRegion.FIXED_INCOME;
import static com.fintex.sm.model.DataProvider.MORNINGSTAR;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_BS_BS_001;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static java.math.BigDecimal.ZERO;

@Service
public class FixedIncomeBondSectorCalculationServiceImpl
    extends
      BreakdownAbstractService<FixedIncomeSectorResult, FixedIncomeSecuritiesAllocationType> {

  public static final Map<FixedIncomeSecuritiesAllocationType, BigDecimal> DEFAULT_MAP = new HashMap<>();

  static final Map<FixedIncomeSecuritiesAllocationType, BigDecimal> ALLOCATION_DEFAULT_MAP;

  static {
    Stream.of(FixedIncomeSecuritiesAllocationType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
    ALLOCATION_DEFAULT_MAP = Collections.unmodifiableMap(
        Stream.of(FixedIncomeSecuritiesAllocationType.values()).collect(java.util.stream.Collectors.toMap(type -> type, type -> ZERO)));
  }

  private final SecurityDataFetcher<FixedIncomeBondSecurities> fixedIncomeBondSectorSecurityDataFetcher;
  private final SecurityDataFetcher<HoldingAssetAllocation> assetAllocationSecurityDataFetcher;
  private final AssetAllocationDataMapper assetAllocationDataMapper;

  public FixedIncomeBondSectorCalculationServiceImpl(
      SecurityDataFetcher<FixedIncomeBondSecurities> fixedIncomeBondSectorSecurityDataFetcher,
      SecurityDataFetcher<HoldingAssetAllocation> assetAllocationSecurityDataFetcher,
      AssetAllocationDataMapper assetAllocationDataMapper) {
    super();
    this.fixedIncomeBondSectorSecurityDataFetcher = fixedIncomeBondSectorSecurityDataFetcher;
    this.assetAllocationSecurityDataFetcher = assetAllocationSecurityDataFetcher;
    this.assetAllocationDataMapper = assetAllocationDataMapper;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.FIXED_INCOME_BOND_SECTOR;
  }

  @Override
  public ExposureDataHolder<FixedIncomeSecuritiesAllocationType> fetchExposures(PortfolioHoldingsCommand reqDTO) {
    Map<Holding, FixedIncomeBondSecurities> rawData = fixedIncomeBondSectorSecurityDataFetcher.fetch(
        reqDTO.getHoldings(), List.of());
    return AllocationMappingUtils.mapTypedAllocations(rawData,
        FixedIncomeBondSecurities::getFixedIncomeBondSectors,
        ALLOCATION_DEFAULT_MAP, WRN_BS_BS_001);
  }

  @Override
  public FixedIncomeSectorResult calculate(ExposureDataHolder<FixedIncomeSecuritiesAllocationType> exposureData,
      List<Holding> holdings) {
    var exposures = exposureData.allocations();
    var warnings = new ArrayList<>(exposureData.warnings());
    if (PortfolioUtils.areAllValuesZerosInMap(exposures)) {
      FixedIncomeSectorResult defaultResult = new FixedIncomeSectorResult();
      defaultResult.setFixedIncomeSector(DEFAULT_MAP);
      defaultResult.setWarnings(warnings);
      return defaultResult;
    }

    Map<Holding, BigDecimal> fixedIncomePlusCash = getFixedIncomePlusCash(holdings, warnings);

    FixedIncomeBondSectorCalculation calculation = new FixedIncomeBondSectorCalculation(exposures, holdings, warnings,
        fixedIncomePlusCash);
    return calculation.calculate();
  }

  private Map<Holding, BigDecimal> getFixedIncomePlusCash(final List<Holding> holdings,
      final List<Warning> warnings) {
    final Map<Holding, HoldingAssetAllocation> rawData = assetAllocationSecurityDataFetcher.fetch(
        holdings, List.of(MORNINGSTAR));
    var assetAllocations = assetAllocationDataMapper.toRegionExposures(rawData);
    return assetAllocations.entrySet()
        .stream()
        .collect(toMap(Map.Entry::getKey, this::getFixedIncomePlusCashValue));
  }

  private BigDecimal getFixedIncomePlusCashValue(Map.Entry<Holding, Map<AssetAllocationRegion, BigDecimal>> entry) {
    BigDecimal fixedIncome = entry.getValue().getOrDefault(FIXED_INCOME, ZERO);
    BigDecimal cash = entry.getValue().getOrDefault(CASH, ZERO);
    return fixedIncome.add(cash);
  }

}
