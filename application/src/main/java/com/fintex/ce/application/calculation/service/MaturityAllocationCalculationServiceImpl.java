package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.application.mapping.response.MaturityAllocationResponseMapper;
import com.fintex.ce.application.util.AllocationMappingUtils;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.allocation.MaturityAllocation;
import com.fintex.ce.model.domain.calculation.allocation.MaturityAllocationType;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.MaturityAllocationResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.model.error.ErrorCode.MISSING_MATURITY_ALLOCATION;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

@Service
public class MaturityAllocationCalculationServiceImpl
    extends
      BreakdownAbstractService<MaturityAllocationResult, MaturityAllocationType> {

  private final SecurityDataFetcher<MaturityAllocation> maturityAllocationSecurityDataFetcher;
  private final MaturityAllocationResponseMapper responseMapper;

  static final Map<MaturityAllocationType, BigDecimal> ALLOCATION_DEFAULT_MAP = Collections.unmodifiableMap(
      Stream.of(MaturityAllocationType.values()).collect(toMap(type -> type, type -> ZERO)));

  public MaturityAllocationCalculationServiceImpl(
      final SecurityDataFetcher<MaturityAllocation> maturityAllocationSecurityDataFetcher,
      final MaturityAllocationResponseMapper responseMapper) {
    super();
    this.maturityAllocationSecurityDataFetcher = maturityAllocationSecurityDataFetcher;
    this.responseMapper = responseMapper;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.MATURITY_ALLOCATION;
  }

  @Override
  public MaturityAllocationResult calculate(ExposureDataHolder<MaturityAllocationType> exposureData,
      List<PortfolioHolding> holdings) {
    var exposures = exposureData.allocations();
    var warnings = new ArrayList<>(exposureData.warnings());
    if (PortfolioUtils.areAllValuesZerosInMap(exposures)) {
      return responseMapper.toEmptyResponse(warnings);
    }
    final Map<MaturityAllocationType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings,
        MaturityAllocationType.values());
    return responseMapper.fromNetProducts(netProducts, warnings);
  }

  @Override
  public ExposureDataHolder<MaturityAllocationType> fetchExposures(PortfolioHoldingsCommand reqDTO) {
    Map<PortfolioHolding, MaturityAllocation> rawData = maturityAllocationSecurityDataFetcher.fetch(reqDTO
        .getHoldings(),
        List.of());
    return AllocationMappingUtils.mapToAllocations(rawData,
        MaturityAllocation::getMaturityDurationValues,
        MaturityAllocationType::fromValue,
        ALLOCATION_DEFAULT_MAP,
        MISSING_MATURITY_ALLOCATION,
        "FDS Get Maturity Allocation",
        (map, entry) -> map.merge(entry.getKey().getDisplayType(), entry.getValue(), BigDecimal::add));
  }
}
