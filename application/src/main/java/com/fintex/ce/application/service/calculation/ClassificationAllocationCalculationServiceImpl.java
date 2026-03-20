package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.service.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.domain.model.enumeration.HoldingType;
import com.fintex.ce.domain.model.calculation.ClassificationAllocationType;
import com.fintex.ce.domain.model.ClassificationAllocation;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.result.ClassificationAllocationResult;
import com.fintex.ce.port.sm.SecurityDataFetcher;
import com.fintex.ce.util.PortfolioUtils;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_CA_CA_001;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_UNKNOWN_001;
import static com.fintex.ce.util.CalculationUtils.reScale;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

@Service
public class ClassificationAllocationCalculationServiceImpl
    extends
      BreakdownAbstractService<ClassificationAllocationResult, ClassificationAllocationType> {

  protected static final Map<ClassificationAllocationType, BigDecimal> DEFAULT_MAP = new EnumMap<>(ClassificationAllocationType.class);

  static final Map<ClassificationAllocationType, BigDecimal> ALLOCATION_DEFAULT_MAP;

  static final Map<HoldingType, ClassificationAllocationType> UNCLASSIFIED_MAP;

  static {
    Stream.of(ClassificationAllocationType.values()).forEach(f -> DEFAULT_MAP.put(f, null));

    ALLOCATION_DEFAULT_MAP = Collections.unmodifiableMap(
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

  private final SecurityDataFetcher<ClassificationAllocation> classificationAllocationSecurityDataFetcher;

  public ClassificationAllocationCalculationServiceImpl(
      final SecurityDataFetcher<ClassificationAllocation> classificationAllocationSecurityDataFetcher) {
    super();
    this.classificationAllocationSecurityDataFetcher = classificationAllocationSecurityDataFetcher;
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
  public Map<Holding, Map<ClassificationAllocationType, BigDecimal>> fetchExposures(
      final PortfolioHoldingsCommand reqDTO,
      final List<Warning> warnings) {
    Map<Holding, ClassificationAllocation> rawData = classificationAllocationSecurityDataFetcher.fetch(
        reqDTO.getHoldings(), List.of());
    return mapToAllocations(rawData, warnings);
  }

  private Map<Holding, Map<ClassificationAllocationType, BigDecimal>> mapToAllocations(
      final Map<Holding, ClassificationAllocation> rawData,
      final List<Warning> warnings) {
    Map<Holding, Map<ClassificationAllocationType, BigDecimal>> result = new HashMap<>();
    rawData.forEach((holding, allocation) -> {
      Map<ClassificationAllocationType, BigDecimal> map = new EnumMap<>(ALLOCATION_DEFAULT_MAP);

      if (Objects.isNull(allocation) || CollectionUtils.isEmpty(allocation.getSecurityClassificationValues())) {
        Optional.ofNullable(holding.getType())
            .map(UNCLASSIFIED_MAP::get)
            .ifPresentOrElse(type -> map.put(type, BigDecimal.ONE),
                () -> warnings.add(WRN_CA_CA_001.warning(holding)));
        result.put(holding, map);
        return;
      }

      allocation.getSecurityClassificationValues()
          .forEach((typeStr, value) -> {
            ClassificationAllocationType type = ClassificationAllocationType.of(typeStr);
            Optional.ofNullable(type)
                .ifPresentOrElse(
                    classificationAllocationType -> map.put(classificationAllocationType, value),
                    () -> warnings.add(
                        WRN_UNKNOWN_001.warning(holding, typeStr, "FDS Get Calculation Allocation")));
          });

      result.put(holding, map);
    });
    return result;
  }

}
