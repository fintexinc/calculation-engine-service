package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.util.AllocationMappingUtils;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.calculation.SingleAttributeCalculationService;
import com.fintex.ce.model.domain.calculation.allocation.ClassificationAllocation;
import com.fintex.ce.model.domain.calculation.allocation.ClassificationAllocationType;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.ClassificationAllocationResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.application.util.CalculationUtils.reScale;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.error.ErrorCode.MISSING_CLASSIFICATION_ALLOCATION;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class ClassificationAllocationService
    extends
      BreakdownAbstractService<Map<PortfolioHolding, ClassificationAllocation>, ClassificationAllocationResult, ClassificationAllocationType>
    implements
      SingleAttributeCalculationService<PortfolioHoldingsCommand, ClassificationAllocation, ClassificationAllocationResult> {

  protected static final Map<ClassificationAllocationType, BigDecimal> DEFAULT_MAP = new EnumMap<>(
      ClassificationAllocationType.class);

  static final Map<ClassificationAllocationType, BigDecimal> ALLOCATION_DEFAULT_MAP;

  static final Map<FinancialInstrumentType, ClassificationAllocationType> UNCLASSIFIED_MAP;

  static {
    Stream.of(ClassificationAllocationType.values()).forEach(f -> DEFAULT_MAP.put(f, null));

    ALLOCATION_DEFAULT_MAP = Collections.unmodifiableMap(
        Stream.of(ClassificationAllocationType.values()).collect(toMap(type -> type, type -> ZERO)));

    UNCLASSIFIED_MAP = Map.of(
        FinancialInstrumentType.MUTUAL_FUND_CANADA, ClassificationAllocationType.UNCLASSIFIED__CANADA,
        FinancialInstrumentType.ETF_CANADA, ClassificationAllocationType.UNCLASSIFIED__UNCLASSIFIED,
        FinancialInstrumentType.ETF_US, ClassificationAllocationType.UNCLASSIFIED__UNCLASSIFIED,
        FinancialInstrumentType.MUTUAL_FUND_US, ClassificationAllocationType.UNCLASSIFIED__US,
        FinancialInstrumentType.STOCK_CANADA, ClassificationAllocationType.EQUITY__UNCLASSIFIED,
        FinancialInstrumentType.STOCK_US, ClassificationAllocationType.EQUITY__UNCLASSIFIED,
        FinancialInstrumentType.FIXED_INCOME, ClassificationAllocationType.FIXED_INCOME__UNCLASSIFIED);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.CLASSIFICATION_ALLOCATION;
  }

  @Override
  public CompositeSecurityAttribute requiredAttribute() {
    return CompositeSecurityAttribute.SECURITY_CLASSIFICATION_ALLOCATION;
  }

  @Override
  public ClassificationAllocationResult perform(PortfolioHoldingsCommand command,
      Map<PortfolioHolding, ClassificationAllocation> data) {
    return calculate(fetchExposures(command, data), command.getHoldings());
  }

  public ClassificationAllocationResult calculate(
      ExposureDataHolder<ClassificationAllocationType> exposureData,
      List<PortfolioHolding> holdings) {
    var exposures = exposureData.allocations();
    var warnings = new ArrayList<>(exposureData.warnings());

    if (PortfolioUtils.areAllValuesZerosInMap(exposures)) {
      return ClassificationAllocationResult.builder()
          .classificationAllocation(DEFAULT_MAP)
          .warnings(warnings)
          .build();
    }

    final Map<ClassificationAllocationType, BigDecimal> netProducts = calculateNetProducts(
        exposures, holdings, ClassificationAllocationType.values());
    final Map<ClassificationAllocationType, BigDecimal> scaledValues = toUserScale(reScale(netProducts));
    return ClassificationAllocationResult.builder()
        .classificationAllocation(scaledValues)
        .warnings(warnings)
        .build();
  }

  public ExposureDataHolder<ClassificationAllocationType> fetchExposures(final PortfolioHoldingsCommand command,
      final Map<PortfolioHolding, ClassificationAllocation> data) {
    Map<PortfolioHolding, ClassificationAllocation> rawData = FilterUtils.restrictToHoldings(data,
        command.getHoldings());
    return AllocationMappingUtils.mapTypedAllocations(rawData,
        ClassificationAllocation::getSecurityClassificationValues,
        ALLOCATION_DEFAULT_MAP, MISSING_CLASSIFICATION_ALLOCATION);
  }

}
