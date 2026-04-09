package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.SalesChargeCalculation;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.SalesCharge;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.SalesChargeResult;
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
    Map<Holding, SalesCharge> salesCharges = salesChargeSecurityDataFetcher.fetch(reqDTO.getHoldings(),
        reqDTO.getDataProviders());

    SalesChargeCalculation salesChargeCalculation = getSalesChargeCalculation(salesCharges);
    return salesChargeCalculation.calculate();
  }

  public SalesChargeCalculation getSalesChargeCalculation(Map<Holding, SalesCharge> salesCharges) {
    return new SalesChargeCalculation(salesCharges);
  }

}
