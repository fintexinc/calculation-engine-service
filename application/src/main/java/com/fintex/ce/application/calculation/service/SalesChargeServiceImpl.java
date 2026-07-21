package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.SalesChargeCalculation;
import com.fintex.ce.calculation.SingleAttributeCalculationService;
import com.fintex.ce.model.domain.calculation.fee.SalesCharge;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.SalesChargeResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;

import java.util.Map;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class SalesChargeServiceImpl
    implements
      SingleAttributeCalculationService<PortfolioHoldingsCommand, SalesCharge, SalesChargeResult> {

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.SALES_CHARGE;
  }

  @Override
  public CompositeSecurityAttribute requiredAttribute() {
    return CompositeSecurityAttribute.SALES_CHARGE;
  }

  @Override
  public SalesChargeResult perform(PortfolioHoldingsCommand command, Map<PortfolioHolding, SalesCharge> data) {
    Map<PortfolioHolding, SalesCharge> salesCharges = FilterUtils.restrictToHoldings(data, command.getHoldings());

    SalesChargeCalculation salesChargeCalculation = getSalesChargeCalculation(salesCharges);
    return salesChargeCalculation.calculate();
  }

  public SalesChargeCalculation getSalesChargeCalculation(Map<PortfolioHolding, SalesCharge> salesCharges) {
    return new SalesChargeCalculation(salesCharges);
  }

}
