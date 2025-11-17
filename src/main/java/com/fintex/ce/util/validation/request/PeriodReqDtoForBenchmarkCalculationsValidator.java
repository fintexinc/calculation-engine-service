package com.fintex.ce.util.validation.request;

import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.util.validation.request.chainofresponsibility.BenchmarksCouldNotBeEmptyReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.ReqValidation;
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
