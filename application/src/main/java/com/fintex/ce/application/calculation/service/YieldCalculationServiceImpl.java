package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.mapping.response.YieldResponseMapper;
import com.fintex.ce.calculation.SingleAttributeCalculationService;
import com.fintex.ce.model.domain.calculation.yield.Yield;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.income.YieldResult;
import com.fintex.ce.model.dto.command.YieldCommand;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.error.Notification;

import java.util.ArrayList;
import java.util.Map;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class YieldCalculationServiceImpl
    implements
      SingleAttributeCalculationService<YieldCommand, Yield, YieldResult> {

  private final YieldResponseMapper responseMapper;

  public YieldCalculationServiceImpl(final YieldResponseMapper responseMapper) {
    this.responseMapper = responseMapper;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.YIELD;
  }

  @Override
  public CompositeSecurityAttribute requiredAttribute() {
    return CompositeSecurityAttribute.INCOME;
  }

  @Override
  public YieldResult perform(final YieldCommand command, final Map<PortfolioHolding, Yield> data) {
    final ArrayList<Notification> warnings = new ArrayList<>();
    final Map<PortfolioHolding, Yield> yieldData = FilterUtils.restrictToHoldings(data, command.getHoldings());
    return responseMapper.toResponse(yieldData, warnings);
  }
}
