package com.fintex.ce.application.service.calculation;

import com.fintex.ce.domain.enumeration.calculation.GeographicRegionType;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.application.result.GeographicExposureResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.cache.FixedIncomeGeographicExposureCacheStorage;
import com.fintex.ce.application.service.calculation.breakdown.BreakdownAbstractService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.PortfolioUtils.areAllValuesInMapEmpty;

@Service
public class FixedIncomeGeographicExposureCalculationImpl
    extends
      BreakdownAbstractService<GeographicExposureResult, GeographicRegionType> {

  private final FixedIncomeGeographicExposureCacheStorage exposureCacheStorage;

  public static final Map<GeographicRegionType, BigDecimal> DEFAULT_MAP = new HashMap<>();

  static {
    Stream.of(GeographicRegionType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
  }

  public FixedIncomeGeographicExposureCalculationImpl(FixedIncomeGeographicExposureCacheStorage exposureCacheStorage) {
    super();
    this.exposureCacheStorage = exposureCacheStorage;
  }

  @Override
  public GeographicExposureResult calculate(Map<Holding, Map<GeographicRegionType, BigDecimal>> exposures,
      List<Holding> holdings,
      List<Warning> warnings) {
    if (areAllValuesInMapEmpty(exposures)) {
      GeographicExposureResult defaultResult = new GeographicExposureResult();
      defaultResult.setEquityGeographicExposure(DEFAULT_MAP);
      defaultResult.setWarnings(warnings);
      return defaultResult;
    }
    Map<GeographicRegionType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings, GeographicRegionType
        .values());
    Map<GeographicRegionType, BigDecimal> rescaledValues = toUserScale(reScaleAbs(netProducts));
    GeographicExposureResult geoResult = new GeographicExposureResult();
    geoResult.setEquityGeographicExposure(rescaledValues);
    geoResult.setWarnings(warnings);
    return geoResult;
  }

  @Override
  public Map<Holding, Map<GeographicRegionType, BigDecimal>> getLoadFromCacheStorage(PortfolioHoldingsCommand reqDTO,
      List<Warning> warnings) {
    return exposureCacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
  }

}
