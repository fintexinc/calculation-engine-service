package com.fintex.ce.application.service.calculation;

import com.fintex.ce.domain.enumeration.calculation.ClassificationAllocationType;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.ClassificationAllocationResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.port.output.cache.HoldingDataLoader;
import com.fintex.ce.application.service.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.util.PortfolioUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.util.CalculationUtils.reScale;
import static com.fintex.ce.util.DecimalUtils.toUserScale;

@Service
public class ClassificationAllocationCalculationServiceImpl
    extends
      BreakdownAbstractService<ClassificationAllocationResult, ClassificationAllocationType> {

  public static final Map<ClassificationAllocationType, BigDecimal> DEFAULT_MAP = new HashMap<>();

  static {
    Stream.of(ClassificationAllocationType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
  }

  private final HoldingDataLoader<Map<Holding, Map<ClassificationAllocationType, BigDecimal>>> cacheStorage;

  public ClassificationAllocationCalculationServiceImpl(      final HoldingDataLoader<Map<Holding, Map<ClassificationAllocationType, BigDecimal>>> cacheStorage) {
    super();
    this.cacheStorage = cacheStorage;
  }

  @Override
  public ClassificationAllocationResult calculate(
      final Map<Holding, Map<ClassificationAllocationType, BigDecimal>> exposures,
      final List<Holding> holdings,
      final List<Warning> warnings) {

    if (PortfolioUtils.areAllValuesZerosInMap(exposures)) {
      ClassificationAllocationResult defaultResult = new ClassificationAllocationResult();
      defaultResult.setClassificationAllocation(DEFAULT_MAP);
      defaultResult.setWarnings(warnings);
      return defaultResult;
    }

    final Map<ClassificationAllocationType, BigDecimal> netProducts = calculateNetProducts(
        exposures, holdings, ClassificationAllocationType.values());
    final Map<ClassificationAllocationType, BigDecimal> scaledValues = toUserScale(reScale(netProducts));
    ClassificationAllocationResult result = new ClassificationAllocationResult();
    result.setClassificationAllocation(scaledValues);
    result.setWarnings(warnings);
    return result;
  }

  @Override
  public Map<Holding, Map<ClassificationAllocationType, BigDecimal>> getLoadFromCacheStorage(
      final PortfolioHoldingsCommand reqDTO,
      final List<Warning> warnings) {
    return cacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
  }

}
