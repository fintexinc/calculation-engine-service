package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.response.MaturityAllocationResponseMapper;
import com.fintex.ce.domain.enumeration.calculation.MaturityAllocationType;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.MaturityAllocationResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.port.output.cache.HoldingDataLoader;
import com.fintex.ce.application.service.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.util.PortfolioUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class MaturityAllocationCalculationServiceImpl
    extends
      BreakdownAbstractService<MaturityAllocationResult, MaturityAllocationType> {

  private final HoldingDataLoader<Map<Holding, Map<MaturityAllocationType, BigDecimal>>> cacheStorage;
  private final MaturityAllocationResponseMapper responseMapper;
  public static final Map<MaturityAllocationType, BigDecimal> DEFAULT_MAP = new EnumMap<>(MaturityAllocationType.class);

  public MaturityAllocationCalculationServiceImpl(      final HoldingDataLoader<Map<Holding, Map<MaturityAllocationType, BigDecimal>>> cacheStorage,
      final MaturityAllocationResponseMapper responseMapper) {
    super();
    this.cacheStorage = cacheStorage;
    this.responseMapper = responseMapper;
  }

  @Override
  public MaturityAllocationResult calculate(Map<Holding, Map<MaturityAllocationType, BigDecimal>> exposures,
      List<Holding> holdings, List<Warning> warnings) {
    if (PortfolioUtils.areAllValuesZerosInMap(exposures)) {
      return responseMapper.toEmptyResponse(warnings);
    }
    final Map<MaturityAllocationType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings,
        MaturityAllocationType.values());
    return responseMapper.fromNetProducts(netProducts, warnings);
  }

  @Override
  public Map<Holding, Map<MaturityAllocationType, BigDecimal>> getLoadFromCacheStorage(PortfolioHoldingsCommand reqDTO,
      List<Warning> warnings) {
    return cacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
  }
}
