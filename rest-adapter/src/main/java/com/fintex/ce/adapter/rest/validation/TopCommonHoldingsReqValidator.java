package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.adapter.rest.dto.request.TopCommonHoldingsReqDTO;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.ReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.TopCommonHoldingsReqValidation;
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
