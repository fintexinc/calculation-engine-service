package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.mapping.response.FixedIncomeStyleboxExposureResponseMapper;
import com.fintex.ce.application.util.AllocationMappingUtils;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.exposure.FixedIncomeStyleboxExposure;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.FixedIncomeStyleboxExposureResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.rating.FixedIncomeStyleBoxType;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.model.error.ErrorCode.MISSING_FIXED_INCOME_STYLEBOX_EXPOSURE;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

@Service
public class FixedIncomeStyleboxExposureService
    extends
      BreakdownAbstractService<FixedIncomeStyleboxExposureResult, FixedIncomeStyleBoxType> {

  static final Map<FixedIncomeStyleBoxType, BigDecimal> DEFAULT_MAP;

  static {
    DEFAULT_MAP = Collections.unmodifiableMap(
        Stream.of(FixedIncomeStyleBoxType.values()).collect(toMap(type -> type, type -> ZERO)));
  }

  private final SecurityDataFetcher<FixedIncomeStyleboxExposure> fixedIncomeStyleboxSecurityDataFetcher;
  private final FixedIncomeStyleboxExposureResponseMapper responseMapper;

  public FixedIncomeStyleboxExposureService(
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
      List<PortfolioHolding> holdings) {
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
  public ExposureDataHolder<FixedIncomeStyleBoxType> fetchExposures(PortfolioHoldingsCommand command) {
    Map<PortfolioHolding, FixedIncomeStyleboxExposure> rawData = fixedIncomeStyleboxSecurityDataFetcher.fetch(
        command.getHoldings(), command.getDataProviders());
    return AllocationMappingUtils.mapTypedAllocations(rawData,
        FixedIncomeStyleboxExposure::getBoxValues,
        DEFAULT_MAP, MISSING_FIXED_INCOME_STYLEBOX_EXPOSURE);
  }
}
