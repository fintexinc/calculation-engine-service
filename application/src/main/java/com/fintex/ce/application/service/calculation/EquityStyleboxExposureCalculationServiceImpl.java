package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.response.EquityStyleboxExposureResponseMapper;
import com.fintex.ce.application.service.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.domain.model.calculation.EquityStyleboxType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.EquityStyleboxExposureResult;
import com.fintex.ce.port.sm.SecurityDataFetcher;
import com.fintex.ce.util.AllocationMappingUtils;
import com.fintex.ce.util.PortfolioUtils;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_ES_ESE_001;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

@Service
public class EquityStyleboxExposureCalculationServiceImpl
    extends
      BreakdownAbstractService<EquityStyleboxExposureResult, EquityStyleboxType> {

  static final Map<EquityStyleboxType, BigDecimal> DEFAULT_MAP;

  static {
    DEFAULT_MAP = Collections.unmodifiableMap(
        Stream.of(EquityStyleboxType.values()).collect(toMap(type -> type, type -> ZERO)));
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
  public EquityStyleboxExposureResult calculate(Map<Holding, Map<EquityStyleboxType, BigDecimal>> exposures,
      List<Holding> holdings, List<Warning> warnings) {
    if (PortfolioUtils.areAllValuesZerosInMap(exposures)) {
      return responseMapper.toEmptyResponse(warnings);
    }
    final Map<EquityStyleboxType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings, EquityStyleboxType
        .values());
    return responseMapper.fromNetProducts(netProducts, warnings);
  }

  @Override
  public Map<Holding, Map<EquityStyleboxType, BigDecimal>> fetchExposures(PortfolioHoldingsCommand reqDTO,
      List<Warning> warnings) {
    Map<Holding, EquityStyleboxExposure> rawData = equityStyleboxSecurityDataFetcher.fetch(reqDTO.getHoldings(),
        List.of());
    return AllocationMappingUtils.mapToAllocations(rawData,
        EquityStyleboxExposure::getBoxValues, EquityStyleboxType::of,
        DEFAULT_MAP, WRN_ES_ESE_001, "FDS Get Equity Stylebox Exposure", warnings);
  }
}
