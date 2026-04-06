package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.application.mapping.response.MaturityAllocationResponseMapper;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.MaturityAllocation;
import com.fintex.ce.domain.model.calculation.MaturityAllocationType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.MaturityAllocationResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.AllocationMappingUtils;
import com.fintex.ce.util.ExposureDataHolder;
import com.fintex.ce.util.PortfolioUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_MA_MA_001;
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
  public MaturityAllocationResult calculate(ExposureDataHolder<MaturityAllocationType> exposureData,
      List<Holding> holdings) {
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
    Map<Holding, MaturityAllocation> rawData = maturityAllocationSecurityDataFetcher.fetch(reqDTO.getHoldings(),
        List.of());
    return AllocationMappingUtils.mapToAllocations(rawData,
        MaturityAllocation::getMaturityDurationValues,
        MaturityAllocationType::fromValue,
        ALLOCATION_DEFAULT_MAP,
        WRN_MA_MA_001,
        "FDS Get Maturity Allocation",
        (map, entry) -> map.merge(entry.getKey().getDisplayType(), entry.getValue(), BigDecimal::add));
  }
}
