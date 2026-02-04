package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.adapter.rest.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotEmptyGicTermReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotNullCashCurrencyValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.ReqValidation;
import org.springframework.stereotype.Component;

@Component
public class ClassificationAllocationReqValidator extends AbstractRequestValidator<PortfolioHoldingsReqDTO> {

  @Override
  public ReqValidation build(PortfolioHoldingsReqDTO reqDTO) {
    return ReqValidation.create()
        .linkWith(new NotNullReqValidation(reqDTO))
        .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
        .linkWith(new NotNullCashCurrencyValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
        .linkWith(new NotEmptyGicTermReqValidator(reqDTO.getHoldings()))
        .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));
  }

}
