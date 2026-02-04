package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.adapter.rest.dto.request.ReturnReqDTO;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpedLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpsdGreaterThanCpedReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpsdLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.ReqValidation;
import org.springframework.stereotype.Component;

@Component
public class ReturnReqDtoValidator extends AbstractRequestValidator<ReturnReqDTO> {

  @Override
  public ReqValidation build(final ReturnReqDTO reqDTO) {
    return ReqValidation.create()
        .linkWith(new NotEmptyCurrencyReqValidator(reqDTO.getCurrency()))
        .linkWith(new CpsdLastDayOfMonthReqValidation(reqDTO.getCustomPerformanceStartDate()))
        .linkWith(new CpedLastDayOfMonthReqValidation(reqDTO.getCustomPerformanceEndDate()))
        .linkWith(new CpsdGreaterThanCpedReqValidation(reqDTO.getCustomPerformanceStartDate(), reqDTO
            .getCustomPerformanceEndDate()))
        .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));
  }
}
