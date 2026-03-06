package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.calculation.SalesChargeCalculation;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.SalesChargeResult;
import com.fintex.ce.domain.model.SalesCharge;
import com.fintex.ce.port.output.cache.HoldingDataLoader;
import com.fintex.ce.service.calculation.CalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SalesChargeServiceImpl implements CalculationService<SalesChargeResult, PortfolioHoldingsCommand> {

  private final HoldingDataLoader<Map<Holding, SalesCharge>> salesChargeCachePort;

  @Autowired
  public SalesChargeServiceImpl(HoldingDataLoader<Map<Holding, SalesCharge>> salesChargeCachePort) {
    this.salesChargeCachePort = salesChargeCachePort;
  }

  @Override
  public SalesChargeResult perform(PortfolioHoldingsCommand reqDTO) {
    Map<Holding, SalesCharge> salesCharges = salesChargeCachePort.load(reqDTO.getHoldings(),
        reqDTO.getDataProviders(), List.of(), new ParamHolderDTO());

    SalesChargeCalculation salesChargeCalculation = getSalesChargeCalculation(salesCharges);
    return salesChargeCalculation.calculate();
  }

  public SalesChargeCalculation getSalesChargeCalculation(Map<Holding, SalesCharge> salesCharges) {
    return new SalesChargeCalculation(salesCharges);
  }

}
