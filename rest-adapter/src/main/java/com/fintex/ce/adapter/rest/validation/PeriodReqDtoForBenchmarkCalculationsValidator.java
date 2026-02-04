package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.adapter.rest.dto.request.PeriodsReqDTO;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.BenchmarksCouldNotBeEmptyReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.ReqValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PeriodReqDtoForBenchmarkCalculationsValidator extends AbstractRequestValidator<PeriodsReqDTO> {

  private final PeriodsReqDtoValidator periodsReqDtoValidator;

  public PeriodReqDtoForBenchmarkCalculationsValidator(@Autowired final PeriodsReqDtoValidator periodsReqDtoValidator) {
    this.periodsReqDtoValidator = periodsReqDtoValidator;
  }

  @Override
  public ReqValidation build(final PeriodsReqDTO reqDTO) {
    return periodsReqDtoValidator.build(reqDTO)
        .linkWith(new BenchmarksCouldNotBeEmptyReqValidation(reqDTO.getBenchmarkHoldings()))
        .linkWith(new HoldingReqValidation(reqDTO.getBenchmarkHoldings()))
        .linkWith(new HoldingValueReqValidator(reqDTO.getBenchmarkHoldings()));
  }
}
