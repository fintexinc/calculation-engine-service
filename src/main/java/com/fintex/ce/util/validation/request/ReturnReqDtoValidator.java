package com.fintex.ce.util.validation.request;

import com.fintex.ce.dto.request.ReturnReqDTO;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpedLastDayOfMonthReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpsdGreaterThanCpedReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpsdLastDayOfMonthReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.ReqValidation;
import org.springframework.stereotype.Component;

@Component
public class ReturnReqDtoValidator extends AbstractRequestValidator<ReturnReqDTO> {

    @Override
    public ReqValidation build(final ReturnReqDTO reqDTO) {
        return ReqValidation.create()
                .linkWith(new NotEmptyCurrencyReqValidator(reqDTO.getCurrency()))
                .linkWith(new CpsdLastDayOfMonthReqValidation(reqDTO.getCustomPerformanceStartDate()))
                .linkWith(new CpedLastDayOfMonthReqValidation(reqDTO.getCustomPerformanceEndDate()))
                .linkWith(new CpsdGreaterThanCpedReqValidation(reqDTO.getCustomPerformanceStartDate(), reqDTO.getCustomPerformanceEndDate()))
                .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));
    }
}
