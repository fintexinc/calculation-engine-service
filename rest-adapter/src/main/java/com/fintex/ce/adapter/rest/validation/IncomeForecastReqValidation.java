package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.adapter.rest.dto.request.IncomeForecastReqDTO;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotEmptyGicTermReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.ReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.TimeIntervalPeriodReqValidation;
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
