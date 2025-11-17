package com.fintex.ce.util.validation.request;

import com.fintex.ce.dto.request.TopCommonHoldingsReqDTO;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.ReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.TopCommonHoldingsReqValidation;
import org.springframework.stereotype.Component;

@Component
public class TopCommonHoldingsReqValidator extends AbstractRequestValidator<TopCommonHoldingsReqDTO> {

    @Override
    public ReqValidation build(final TopCommonHoldingsReqDTO reqDTO) {
        return ReqValidation.create()
                .linkWith(new NotNullReqValidation(reqDTO))
                .linkWith(new TopCommonHoldingsReqValidation(reqDTO))
                .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));
    }
}
