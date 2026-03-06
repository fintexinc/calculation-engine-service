package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.response.YieldResponseMapper;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.YieldCommand;
import com.fintex.ce.port.input.result.YieldResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.port.output.cache.HoldingDataLoader;
import com.fintex.ce.service.calculation.CalculationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class YieldCalculationServiceImpl implements CalculationService<YieldResult, YieldCommand> {

  private final HoldingDataLoader<Map<Holding, Yield>> yieldCachePort;
  private final YieldResponseMapper responseMapper;

  public YieldCalculationServiceImpl(final HoldingDataLoader<Map<Holding, Yield>> yieldCachePort,
      final YieldResponseMapper responseMapper) {
    this.yieldCachePort = yieldCachePort;
    this.responseMapper = responseMapper;
  }

  @Override
  public YieldResult perform(final YieldCommand reqDTO) {
    final ArrayList<Warning> warnings = new ArrayList<>();
    final Map<Holding, Yield> yieldData = yieldCachePort.load(
        reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
    return responseMapper.toResponse(yieldData, warnings);
  }
}
