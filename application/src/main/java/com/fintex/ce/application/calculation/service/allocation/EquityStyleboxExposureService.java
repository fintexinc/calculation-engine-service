package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.mapping.response.EquityStyleboxExposureResponseMapper;
import com.fintex.ce.application.util.AllocationMappingUtils;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.calculation.SingleAttributeCalculationService;
import com.fintex.ce.model.domain.calculation.exposure.EquityStyleboxExposure;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.EquityStyleboxExposureResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.rating.StyleBoxType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.model.error.ErrorCode.MISSING_EQUITY_STYLEBOX_EXPOSURE;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class EquityStyleboxExposureService
    extends
      BreakdownAbstractService<Map<PortfolioHolding, EquityStyleboxExposure>, EquityStyleboxExposureResult, StyleBoxType>
    implements
      SingleAttributeCalculationService<PortfolioHoldingsCommand, EquityStyleboxExposure, EquityStyleboxExposureResult> {

  static final Map<StyleBoxType, BigDecimal> DEFAULT_MAP;

  static {
    DEFAULT_MAP = Collections.unmodifiableMap(
        Stream.of(StyleBoxType.values()).collect(toMap(type -> type, type -> ZERO)));
  }

  private final EquityStyleboxExposureResponseMapper responseMapper;

  public EquityStyleboxExposureService(final EquityStyleboxExposureResponseMapper responseMapper) {
    super();
    this.responseMapper = responseMapper;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.EQUITY_STYLEBOX_EXPOSURE;
  }

  @Override
  public CompositeSecurityAttribute requiredAttribute() {
    return CompositeSecurityAttribute.EQUITY_STYLEBOX;
  }

  @Override
  public EquityStyleboxExposureResult perform(PortfolioHoldingsCommand command,
      Map<PortfolioHolding, EquityStyleboxExposure> data) {
    return calculate(fetchExposures(command, data), command.getHoldings());
  }

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

  public ExposureDataHolder<StyleBoxType> fetchExposures(PortfolioHoldingsCommand command,
      Map<PortfolioHolding, EquityStyleboxExposure> data) {
    Map<PortfolioHolding, EquityStyleboxExposure> rawData = FilterUtils.restrictToHoldings(data,
        command.getHoldings());
    return AllocationMappingUtils.mapTypedAllocations(rawData,
        EquityStyleboxExposure::getBoxValues,
        DEFAULT_MAP, MISSING_EQUITY_STYLEBOX_EXPOSURE);
  }
}
