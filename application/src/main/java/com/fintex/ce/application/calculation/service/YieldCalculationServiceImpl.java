package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.mapping.response.YieldResponseMapper;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.yield.Yield;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.income.YieldResult;
import com.fintex.ce.model.dto.command.YieldCommand;
import com.fintex.ce.model.error.Warning;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;

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
  public CalculationMetric getMetric() {
    return CalculationMetric.YIELD;
  }

  @Override
  public YieldResult perform(final YieldCommand reqDTO) {
    final ArrayList<Warning> warnings = new ArrayList<>();
    final Map<PortfolioHolding, Yield> yieldData = yieldSecurityDataFetcher.fetch(reqDTO.getHoldings(), List.of());
    return responseMapper.toResponse(yieldData, warnings);
  }
}
