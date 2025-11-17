package com.fintex.ce.util.validation.request;

import com.fintex.ce.dto.request.IncomeForecastReqDTO;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotEmptyGicTermReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.ReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.TimeIntervalPeriodReqValidation;
import org.springframework.stereotype.Component;

@Component
public class IncomeForecastReqValidation extends AbstractRequestValidator<IncomeForecastReqDTO> {

    @Override
    public ReqValidation build(final IncomeForecastReqDTO reqDTO) {
        return ReqValidation.create()
                .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
                .linkWith(new NotEmptyGicTermReqValidator(reqDTO.getHoldings()))
                .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()))
                .linkWith(new TimeIntervalPeriodReqValidation(reqDTO.getTimeIntervalPeriods()));
    }

}
