package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.model.domain.calculation.allocation.ClassificationAllocation;
import com.fintex.ce.model.domain.calculation.allocation.ClassificationAllocationType;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.ClassificationAllocationResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.AllocationMappingUtils;
import com.fintex.ce.util.ExposureDataHolder;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.model.error.ErrorCode.WRN_CA_CA_001;
import static com.fintex.ce.util.CalculationUtils.reScale;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

@Service
public class ClassificationAllocationCalculationServiceImpl
    extends
      BreakdownAbstractService<ClassificationAllocationResult, ClassificationAllocationType> {

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

  private final SecurityDataFetcher<ClassificationAllocation> classificationAllocationSecurityDataFetcher;

  public ClassificationAllocationCalculationServiceImpl(
      final SecurityDataFetcher<ClassificationAllocation> classificationAllocationSecurityDataFetcher) {
    super();
    this.classificationAllocationSecurityDataFetcher = classificationAllocationSecurityDataFetcher;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.CLASSIFICATION_ALLOCATION;
  }

  @Override
  public ClassificationAllocationResult calculate(
      ExposureDataHolder<ClassificationAllocationType> exposureData,
      List<PortfolioHolding> holdings) {
    var exposures = exposureData.allocations();
    var warnings = new ArrayList<>(exposureData.warnings());

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
  public ExposureDataHolder<ClassificationAllocationType> fetchExposures(final PortfolioHoldingsCommand reqDTO) {
    Map<PortfolioHolding, ClassificationAllocation> rawData = classificationAllocationSecurityDataFetcher.fetch(
        reqDTO.getHoldings(), List.of());
    return AllocationMappingUtils.mapTypedAllocations(rawData,
        ClassificationAllocation::getSecurityClassificationValues,
        ALLOCATION_DEFAULT_MAP, WRN_CA_CA_001);
  }

}
