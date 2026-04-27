package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.application.mapping.response.EquityStyleboxExposureResponseMapper;
import com.fintex.ce.application.util.AllocationMappingUtils;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.exposure.EquityStyleboxExposure;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.EquityStyleboxExposureResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.rating.StyleBoxType;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.model.error.ErrorCode.MISSING_EQUITY_STYLEBOX_EXPOSURE;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

@Service
public class EquityStyleboxExposureCalculationServiceImpl
    extends
      BreakdownAbstractService<EquityStyleboxExposureResult, StyleBoxType> {

  static final Map<StyleBoxType, BigDecimal> DEFAULT_MAP;

  static {
    DEFAULT_MAP = Collections.unmodifiableMap(
        Stream.of(StyleBoxType.values()).collect(toMap(type -> type, type -> ZERO)));
  }

  private final SecurityDataFetcher<EquityStyleboxExposure> equityStyleboxSecurityDataFetcher;
  private final EquityStyleboxExposureResponseMapper responseMapper;

  public EquityStyleboxExposureCalculationServiceImpl(
      final SecurityDataFetcher<EquityStyleboxExposure> equityStyleboxSecurityDataFetcher,
      final EquityStyleboxExposureResponseMapper responseMapper) {
    super();
    this.equityStyleboxSecurityDataFetcher = equityStyleboxSecurityDataFetcher;
    this.responseMapper = responseMapper;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.EQUITY_STYLEBOX_EXPOSURE;
  }

  @Override
  public EquityStyleboxExposureResult calculate(ExposureDataHolder<StyleBoxType> exposureData,
      List<PortfolioHolding> holdings) {
    var exposures = exposureData.allocations();
    var warnings = new ArrayList<>(exposureData.warnings());
    if (PortfolioUtils.areAllValuesZerosInMap(exposures)) {
      return responseMapper.toEmptyResponse(warnings);
    }
    final Map<StyleBoxType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings, StyleBoxType
        .values());
    return responseMapper.fromNetProducts(netProducts, warnings);
  }

  @Override
  public ExposureDataHolder<StyleBoxType> fetchExposures(PortfolioHoldingsCommand reqDTO) {
    Map<PortfolioHolding, EquityStyleboxExposure> rawData = equityStyleboxSecurityDataFetcher.fetch(reqDTO
        .getHoldings(),
        List.of());
    return AllocationMappingUtils.mapTypedAllocations(rawData,
        EquityStyleboxExposure::getBoxValues,
        DEFAULT_MAP, MISSING_EQUITY_STYLEBOX_EXPOSURE);
  }
}
