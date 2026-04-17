package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.SalesChargeCalculation;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.fee.SalesCharge;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.SalesChargeResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SalesChargeServiceImpl implements CalculationService<SalesChargeResult, PortfolioHoldingsCommand> {

  private final SecurityDataFetcher<SalesCharge> salesChargeSecurityDataFetcher;

  public SalesChargeServiceImpl(SecurityDataFetcher<SalesCharge> salesChargeSecurityDataFetcher) {
    this.salesChargeSecurityDataFetcher = salesChargeSecurityDataFetcher;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.SALES_CHARGE;
  }

  @Override
  public SalesChargeResult perform(PortfolioHoldingsCommand reqDTO) {
    Map<PortfolioHolding, SalesCharge> salesCharges = salesChargeSecurityDataFetcher.fetch(reqDTO.getHoldings(),
        reqDTO.getDataProviders());

    SalesChargeCalculation salesChargeCalculation = getSalesChargeCalculation(salesCharges);
    return salesChargeCalculation.calculate();
  }

  public SalesChargeCalculation getSalesChargeCalculation(Map<PortfolioHolding, SalesCharge> salesCharges) {
    return new SalesChargeCalculation(salesCharges);
  }

}
