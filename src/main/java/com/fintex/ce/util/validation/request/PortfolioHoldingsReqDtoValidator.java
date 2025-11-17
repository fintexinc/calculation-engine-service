package com.fintex.ce.util.validation.request;

import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotEmptyGicTermReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.ReqValidation;
import org.springframework.stereotype.Component;

@Component
public class PortfolioHoldingsReqDtoValidator extends AbstractRequestValidator<PortfolioHoldingsReqDTO> {

    @Override
    public ReqValidation build(PortfolioHoldingsReqDTO reqDTO) {
        return ReqValidation.create()
                .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
                .linkWith(new NotEmptyGicTermReqValidator(reqDTO.getHoldings()))
                .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));
    }
}
