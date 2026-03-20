package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.response.YieldResponseMapper;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.dto.command.YieldCommand;
import com.fintex.ce.domain.model.result.YieldResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.port.sm.SecurityDataFetcher;
import com.fintex.ce.service.calculation.CalculationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class YieldCalculationServiceImpl implements CalculationService<YieldResult, YieldCommand> {

  private final SecurityDataFetcher<Yield> yieldSecurityDataFetcher;
  private final YieldResponseMapper responseMapper;

  public YieldCalculationServiceImpl(final SecurityDataFetcher<Yield> yieldSecurityDataFetcher,
      final YieldResponseMapper responseMapper) {
    this.yieldSecurityDataFetcher = yieldSecurityDataFetcher;
    this.responseMapper = responseMapper;
  }

  @Override
  public YieldResult perform(final YieldCommand reqDTO) {
    final ArrayList<Warning> warnings = new ArrayList<>();
    final Map<Holding, Yield> yieldData = yieldSecurityDataFetcher.fetch(reqDTO.getHoldings(), List.of());
    return responseMapper.toResponse(yieldData, warnings);
  }
}
