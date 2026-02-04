package com.fintex.ce.application.service.calculation;

import com.fintex.ce.domain.enumeration.calculation.CountryRegionType;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.application.result.EquityCountryExposureResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.cache.EquityCountryAllocationCacheStorage;
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
public class EquityCountryExposureCalculationServiceImpl
    extends
      BreakdownAbstractService<EquityCountryExposureResult, CountryRegionType> {

  private final EquityCountryAllocationCacheStorage countryAllocationCacheStorage;

  public static final Map<CountryRegionType, BigDecimal> DEFAULT_MAP = new HashMap<>();

  static {
    Stream.of(CountryRegionType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
  }

  public EquityCountryExposureCalculationServiceImpl(
      final EquityCountryAllocationCacheStorage countryAllocationCacheStorage) {
    super();
    this.countryAllocationCacheStorage = countryAllocationCacheStorage;
  }

  @Override
  public EquityCountryExposureResult calculate(final Map<Holding, Map<CountryRegionType, BigDecimal>> exposures,
      final List<Holding> holdings,
      final List<Warning> warnings) {
    if (areAllValuesInMapEmpty(exposures)) {
      EquityCountryExposureResult defaultResult = new EquityCountryExposureResult();
      defaultResult.setEquityCountryExposure(DEFAULT_MAP);
      defaultResult.setWarnings(warnings);
      return defaultResult;
    }
    final Map<CountryRegionType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings, CountryRegionType
        .values());
    final Map<CountryRegionType, BigDecimal> scaledValues = toUserScale(reScaleAbs(netProducts));
    EquityCountryExposureResult result = new EquityCountryExposureResult();
    result.setEquityCountryExposure(scaledValues);
    result.setWarnings(warnings);
    return result;
  }

  @Override
  public Map<Holding, Map<CountryRegionType, BigDecimal>> getLoadFromCacheStorage(final PortfolioHoldingsCommand reqDTO,
      final List<Warning> warnings) {
    return countryAllocationCacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
  }

}
