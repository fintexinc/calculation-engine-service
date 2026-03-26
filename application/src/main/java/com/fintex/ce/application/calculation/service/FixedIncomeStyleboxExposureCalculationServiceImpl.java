package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.application.mapping.response.FixedIncomeStyleboxExposureResponseMapper;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.FixedIncomeStyleboxExposure;
import com.fintex.ce.domain.model.calculation.FixedIncomeStyleboxType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.FixedIncomeStyleboxExposureResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.AllocationMappingUtils;
import com.fintex.ce.util.PortfolioUtils;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_FIS_FISE_001;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

@Service
public class FixedIncomeStyleboxExposureCalculationServiceImpl
    extends
      BreakdownAbstractService<FixedIncomeStyleboxExposureResult, FixedIncomeStyleboxType> {

  static final Map<FixedIncomeStyleboxType, BigDecimal> DEFAULT_MAP;

  static {
    DEFAULT_MAP = Collections.unmodifiableMap(
        Stream.of(FixedIncomeStyleboxType.values()).collect(toMap(type -> type, type -> ZERO)));
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
  public FixedIncomeStyleboxExposureResult calculate(Map<Holding, Map<FixedIncomeStyleboxType, BigDecimal>> exposures,
      List<Holding> holdings, List<Warning> warnings) {
    if (PortfolioUtils.areAllValuesZerosInMap(exposures)) {
      return responseMapper.toEmptyResponse(warnings);
    }
    final Map<FixedIncomeStyleboxType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings,
        FixedIncomeStyleboxType.values());
    return responseMapper.fromNetProducts(netProducts, warnings);
  }

  @Override
  public Map<Holding, Map<FixedIncomeStyleboxType, BigDecimal>> fetchExposures(PortfolioHoldingsCommand reqDTO,
      List<Warning> warnings) {
    Map<Holding, FixedIncomeStyleboxExposure> rawData = fixedIncomeStyleboxSecurityDataFetcher.fetch(
        reqDTO.getHoldings(), List.of());
    return AllocationMappingUtils.mapToAllocations(rawData,
        FixedIncomeStyleboxExposure::getBoxValues, FixedIncomeStyleboxType::of,
        DEFAULT_MAP, WRN_FIS_FISE_001, "FDS Get Fixed Income Stylebox Exposure", warnings);
  }
}
