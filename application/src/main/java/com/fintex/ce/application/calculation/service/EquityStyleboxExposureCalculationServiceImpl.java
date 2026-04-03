package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.application.mapping.response.EquityStyleboxExposureResponseMapper;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.EquityStyleboxExposureResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.AllocationMappingUtils;
import com.fintex.ce.util.ExposureDataHolder;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.sm.model.domain.enumeration.StyleBoxType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_ES_ESE_001;
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
      List<Holding> holdings) {
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
    Map<Holding, EquityStyleboxExposure> rawData = equityStyleboxSecurityDataFetcher.fetch(reqDTO.getHoldings(),
        List.of());
    return AllocationMappingUtils.mapTypedAllocations(rawData,
        EquityStyleboxExposure::getBoxValues,
        DEFAULT_MAP, WRN_ES_ESE_001);
  }
}
