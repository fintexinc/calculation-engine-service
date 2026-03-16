package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.response.CountryExposureResponseMapper;
import com.fintex.ce.application.service.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.domain.enumeration.calculation.CountryRegionType;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.CountryExposureResult;
import com.fintex.ce.port.output.HoldingDataLoader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.fintex.ce.util.PortfolioUtils.areAllValuesInMapEmpty;

@Service
public class CountryExposureCalculationImpl extends BreakdownAbstractService<CountryExposureResult, CountryRegionType> {

  private final HoldingDataLoader<Map<Holding, Map<CountryRegionType, BigDecimal>>> exposureCacheStorage;
  private final CountryExposureResponseMapper responseMapper;

  public static final Map<CountryRegionType, BigDecimal> DEFAULT_MAP = new HashMap<>();

  public CountryExposureCalculationImpl(@Qualifier("countryExposure") HoldingDataLoader<Map<Holding, Map<CountryRegionType, BigDecimal>>> exposureCacheStorage,
      CountryExposureResponseMapper responseMapper) {
    super();
    this.exposureCacheStorage = exposureCacheStorage;
    this.responseMapper = responseMapper;
  }

  @Override
  public CountryExposureResult calculate(Map<Holding, Map<CountryRegionType, BigDecimal>> exposures,
      List<Holding> holdings,
      List<Warning> warnings) {
    if (areAllValuesInMapEmpty(exposures)) {
      return responseMapper.toEmptyResponse(warnings);
    }
    Map<CountryRegionType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings, CountryRegionType
        .values());
    return responseMapper.fromNetProducts(netProducts, warnings);
  }

  @Override
  public Map<Holding, Map<CountryRegionType, BigDecimal>> fetchExposures(PortfolioHoldingsCommand reqDTO,
      List<Warning> warnings) {
    return exposureCacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
  }
}
