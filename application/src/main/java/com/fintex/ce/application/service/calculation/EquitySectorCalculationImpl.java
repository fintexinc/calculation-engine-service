package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.response.EquitySectorResponseMapper;
import com.fintex.ce.application.service.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.domain.enumeration.calculation.EquitySectorAllocationType;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.EquitySectorResult;
import com.fintex.ce.port.output.HoldingDataLoader;
import com.fintex.ce.util.PortfolioUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class EquitySectorCalculationImpl
    extends
      BreakdownAbstractService<EquitySectorResult, EquitySectorAllocationType> {

  private final HoldingDataLoader<Map<Holding, Map<EquitySectorAllocationType, BigDecimal>>> equitySectorCachePort;
  private final EquitySectorResponseMapper responseMapper;

  public EquitySectorCalculationImpl(final HoldingDataLoader<Map<Holding, Map<EquitySectorAllocationType, BigDecimal>>> equitySectorCachePort,
      final EquitySectorResponseMapper responseMapper) {
    super();
    this.equitySectorCachePort = equitySectorCachePort;
    this.responseMapper = responseMapper;
  }

  @Override
  public EquitySectorResult calculate(final Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> sectors,
      final List<Holding> holdings,
      final List<Warning> warnings) {
    if (PortfolioUtils.areAllValuesZerosInMap(sectors)) {
      return responseMapper.toEmptyResponse(warnings);
    }
    final Map<EquitySectorAllocationType, BigDecimal> netProducts = calculateNetProducts(sectors, holdings,
        EquitySectorAllocationType.values());
    return responseMapper.fromNetProducts(netProducts, warnings);
  }

  @Override
  public Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> fetchExposures(
      final PortfolioHoldingsCommand reqDTO,
      final List<Warning> warnings) {
    return equitySectorCachePort.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
  }
}
