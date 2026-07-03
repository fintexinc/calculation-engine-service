package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.SalesChargeCalculation;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.fee.SalesCharge;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.SalesChargeResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;

import java.util.Map;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class SalesChargeServiceImpl implements CalculationService<PortfolioHoldingsCommand, SalesChargeResult> {

  private final SecurityDataFetcher<SalesCharge> salesChargeSecurityDataFetcher;

  public SalesChargeServiceImpl(SecurityDataFetcher<SalesCharge> salesChargeSecurityDataFetcher) {
    this.salesChargeSecurityDataFetcher = salesChargeSecurityDataFetcher;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.SALES_CHARGE;
  }

  @Override
  public SalesChargeResult perform(PortfolioHoldingsCommand command) {
    Map<PortfolioHolding, SalesCharge> salesCharges = salesChargeSecurityDataFetcher.fetch(command.getHoldings(),
        command.getDataProviders());

    SalesChargeCalculation salesChargeCalculation = getSalesChargeCalculation(salesCharges);
    return salesChargeCalculation.calculate();
  }

  public SalesChargeCalculation getSalesChargeCalculation(Map<PortfolioHolding, SalesCharge> salesCharges) {
    return new SalesChargeCalculation(salesCharges);
  }

}
