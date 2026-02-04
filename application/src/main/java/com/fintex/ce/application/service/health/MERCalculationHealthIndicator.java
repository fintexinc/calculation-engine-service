package com.fintex.ce.application.service.health;

import com.fintex.ce.domain.enumeration.ParameterType;
import com.fintex.ce.application.command.AverageMerCommand;
import com.fintex.ce.application.result.AverageMerResult;
import com.fintex.ce.application.service.calculation.MERCalculationServiceImpl;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("merCalculation")
public class MERCalculationHealthIndicator extends CalculationHeathIndicator<AverageMerCommand> {

  private final MERCalculationServiceImpl merCalculationService;

  public MERCalculationHealthIndicator(final MERCalculationServiceImpl merCalculationService) {
    this.merCalculationService = merCalculationService;
  }

  @Override
  public AverageMerResult calculateResponse(final AverageMerCommand request) {
    return merCalculationService.perform(request);
  }

  @Override
  public AverageMerCommand buildInput() {
    final AverageMerCommand averageMerRequest = new AverageMerCommand();
    averageMerRequest.setHoldings(getHoldings());
    averageMerRequest.setParameterTypes(List.of(ParameterType.ABSOLUTE));
    return averageMerRequest;
  }

}
