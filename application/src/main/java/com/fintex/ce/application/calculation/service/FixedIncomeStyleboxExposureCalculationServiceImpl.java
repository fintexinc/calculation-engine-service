package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.application.mapping.response.FixedIncomeStyleboxExposureResponseMapper;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.FixedIncomeStyleboxExposure;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.FixedIncomeStyleboxExposureResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.AllocationMappingUtils;
import com.fintex.ce.util.ExposureDataHolder;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.sm.model.domain.enumeration.FixedIncomeStyleBoxType;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_FIS_FISE_001;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

@Service
public class FixedIncomeStyleboxExposureCalculationServiceImpl
    extends
      BreakdownAbstractService<FixedIncomeStyleboxExposureResult, FixedIncomeStyleBoxType> {

  static final Map<FixedIncomeStyleBoxType, BigDecimal> DEFAULT_MAP;

  static {
    DEFAULT_MAP = Collections.unmodifiableMap(
        Stream.of(FixedIncomeStyleBoxType.values()).collect(toMap(type -> type, type -> ZERO)));
  }

  private final SecurityDataFetcher<FixedIncomeStyleboxExposure> fixedIncomeStyleboxSecurityDataFetcher;
  private final FixedIncomeStyleboxExposureResponseMapper responseMapper;

  public FixedIncomeStyleboxExposureCalculationServiceImpl(
      final SecurityDataFetcher<FixedIncomeStyleboxExposure> fixedIncomeStyleboxSecurityDataFetcher,
      final FixedIncomeStyleboxExposureResponseMapper responseMapper) {
    super();
    this.fixedIncomeStyleboxSecurityDataFetcher = fixedIncomeStyleboxSecurityDataFetcher;
    this.responseMapper = responseMapper;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.FIXED_INCOME_STYLEBOX_EXPOSURE;
  }

  @Override
  public FixedIncomeStyleboxExposureResult calculate(ExposureDataHolder<FixedIncomeStyleBoxType> exposureData,
      List<Holding> holdings) {
    var exposures = exposureData.allocations();
    var warnings = new ArrayList<>(exposureData.warnings());
    if (PortfolioUtils.areAllValuesZerosInMap(exposures)) {
      return responseMapper.toEmptyResponse(warnings);
    }
    final Map<FixedIncomeStyleBoxType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings,
        FixedIncomeStyleBoxType.values());
    return responseMapper.fromNetProducts(netProducts, warnings);
  }

  @Override
  public ExposureDataHolder<FixedIncomeStyleBoxType> fetchExposures(PortfolioHoldingsCommand reqDTO) {
    Map<Holding, FixedIncomeStyleboxExposure> rawData = fixedIncomeStyleboxSecurityDataFetcher.fetch(
        reqDTO.getHoldings(), List.of());
    return AllocationMappingUtils.mapTypedAllocations(rawData,
        FixedIncomeStyleboxExposure::getBoxValues,
        DEFAULT_MAP, WRN_FIS_FISE_001);
  }
}
