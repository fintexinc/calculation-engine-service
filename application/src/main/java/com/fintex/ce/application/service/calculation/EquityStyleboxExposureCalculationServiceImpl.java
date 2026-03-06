package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.response.EquityStyleboxExposureResponseMapper;
import com.fintex.ce.domain.enumeration.calculation.EquityStyleboxType;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.EquityStyleboxExposureResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.port.output.cache.HoldingDataLoader;
import com.fintex.ce.application.service.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.util.PortfolioUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class EquityStyleboxExposureCalculationServiceImpl
    extends
      BreakdownAbstractService<EquityStyleboxExposureResult, EquityStyleboxType> {

  private final HoldingDataLoader<Map<Holding, Map<EquityStyleboxType, BigDecimal>>> cacheStorage;
  private final EquityStyleboxExposureResponseMapper responseMapper;

  public EquityStyleboxExposureCalculationServiceImpl(      final HoldingDataLoader<Map<Holding, Map<EquityStyleboxType, BigDecimal>>> cacheStorage,
      final EquityStyleboxExposureResponseMapper responseMapper) {
    super();
    this.cacheStorage = cacheStorage;
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
  public Map<Holding, Map<EquityStyleboxType, BigDecimal>> getLoadFromCacheStorage(PortfolioHoldingsCommand reqDTO,
      List<Warning> warnings) {
    return cacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
  }
}
