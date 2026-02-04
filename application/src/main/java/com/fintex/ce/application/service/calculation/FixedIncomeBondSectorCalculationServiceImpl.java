package com.fintex.ce.application.service.calculation;

import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.enumeration.calculation.FixedIncomeSectorType;
import com.fintex.ce.application.calculation.FixedIncomeBondSectorCalculation;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.calculation.AssetAllocationDataDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.application.mapper.AssetAllocationDataMapper;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.application.result.FixedIncomeSectorResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.cache.AssetAllocationCacheStorage;
import com.fintex.ce.adapter.cache.FixedIncomeBondSectorCacheStorage;
import com.fintex.ce.application.service.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.validation.data.AssetAllocationDataValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.domain.enumeration.DataProvider.EAGLE;
import static com.fintex.ce.domain.enumeration.DataProvider.MORNINGSTAR;
import static com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion.CASH;
import static com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion.FIXED_INCOME;
import static com.fintex.ce.util.CollectorUtils.toMap;

@Service
public class FixedIncomeBondSectorCalculationServiceImpl
    extends
      BreakdownAbstractService<FixedIncomeSectorResult, FixedIncomeSectorType> {

  public static final Map<FixedIncomeSectorType, BigDecimal> DEFAULT_MAP = new HashMap<>();

  static {
    Stream.of(FixedIncomeSectorType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
  }

  private final FixedIncomeBondSectorCacheStorage fixedIncomeBondSectorCacheStorage;
  private final AssetAllocationCacheStorage assetAllocationCacheStorage;
  private final AssetAllocationDataMapper assetAllocationDataMapper;
  private final AssetAllocationDataValidator assetAllocationDataValidator;

  @Autowired
  public FixedIncomeBondSectorCalculationServiceImpl(
      FixedIncomeBondSectorCacheStorage fixedIncomeBondSectorCacheStorage,      AssetAllocationCacheStorage assetAllocationCacheStorage,
      AssetAllocationDataMapper assetAllocationDataMapper,
      AssetAllocationDataValidator assetAllocationDataValidator) {
    super();
    this.fixedIncomeBondSectorCacheStorage = fixedIncomeBondSectorCacheStorage;
    this.assetAllocationCacheStorage = assetAllocationCacheStorage;
    this.assetAllocationDataMapper = assetAllocationDataMapper;
    this.assetAllocationDataValidator = assetAllocationDataValidator;
  }

  @Override
  public Map<Holding, Map<FixedIncomeSectorType, BigDecimal>> getLoadFromCacheStorage(PortfolioHoldingsCommand reqDTO,
      List<Warning> warnings) {
    return fixedIncomeBondSectorCacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
  }

  @Override
  public FixedIncomeSectorResult calculate(Map<Holding, Map<FixedIncomeSectorType, BigDecimal>> exposures,
      List<Holding> holdings,
      List<Warning> warnings) {
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
    final AssetAllocationDataDTO assetAllocationDataDto = assetAllocationCacheStorage.load(
        holdings, List.of(MORNINGSTAR, EAGLE), warnings, new ParamHolderDTO());
    assetAllocationDataValidator.validate(assetAllocationDataDto, warnings);
    var assetAllocations = assetAllocationDataMapper.mapForAA(assetAllocationDataDto);
    return assetAllocations.entrySet()
        .stream()
        .collect(toMap(Map.Entry::getKey, this::getFixedIncomePlusCashValue));
  }

  private BigDecimal getFixedIncomePlusCashValue(Map.Entry<Holding, Map<AssetAllocationRegion, BigDecimal>> entry) {
    BigDecimal fixedIncome = entry.getValue().get(FIXED_INCOME);
    BigDecimal cash = entry.getValue().get(CASH);
    return fixedIncome.add(cash);
  }

}
