package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.response.FixedIncomeStyleboxExposureResponseMapper;
import com.fintex.ce.domain.enumeration.calculation.FixedIncomeStyleboxType;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.application.result.FixedIncomeStyleboxExposureResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.cache.FixedIncomeStyleboxExposureCacheStorage;
import com.fintex.ce.application.service.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.util.PortfolioUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class FixedIncomeStyleboxExposureCalculationServiceImpl
    extends
      BreakdownAbstractService<FixedIncomeStyleboxExposureResult, FixedIncomeStyleboxType> {

  private final FixedIncomeStyleboxExposureCacheStorage cacheStorage;
  private final FixedIncomeStyleboxExposureResponseMapper responseMapper;

  public FixedIncomeStyleboxExposureCalculationServiceImpl(      final FixedIncomeStyleboxExposureCacheStorage cacheStorage,
      final FixedIncomeStyleboxExposureResponseMapper responseMapper) {
    super();
    this.cacheStorage = cacheStorage;
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
  public Map<Holding, Map<FixedIncomeStyleboxType, BigDecimal>> getLoadFromCacheStorage(PortfolioHoldingsCommand reqDTO,
      List<Warning> warnings) {
    return cacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
  }
}
