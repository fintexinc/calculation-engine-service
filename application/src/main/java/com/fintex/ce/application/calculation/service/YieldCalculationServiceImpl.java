package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.mapping.response.YieldResponseMapper;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.domain.dto.command.YieldCommand;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.YieldResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

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
  public CalculationMetric getMetric() {
    return CalculationMetric.YIELD;
  }

  @Override
  public YieldResult perform(final YieldCommand reqDTO) {
    final ArrayList<Warning> warnings = new ArrayList<>();
    final Map<Holding, Yield> yieldData = yieldSecurityDataFetcher.fetch(reqDTO.getHoldings(), List.of());
    return responseMapper.toResponse(yieldData, warnings);
  }
}
