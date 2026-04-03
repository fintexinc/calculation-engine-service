package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CipsdGreaterThanCpedReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CipsdLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpedLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodContainYearToDateReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodLessThan12ReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.ReqValidation;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import org.springframework.stereotype.Component;

@Component
public class CorrelationReqValidator extends AbstractRequestValidator<PeriodCommand> {

  @Override
  public ReqValidation build(final PeriodCommand reqDTO) {
    return ReqValidation.create()
        .linkWith(new NotNullReqValidation(reqDTO))
        .linkWith(new NotEmptyCurrencyReqValidator(reqDTO.getCurrency()))
        .linkWith(new CipsdLastDayOfMonthReqValidation(reqDTO.getCustomIntervalPsd()))
        .linkWith(new CpedLastDayOfMonthReqValidation(reqDTO.getCustomPed()))
        .linkWith(new CipsdGreaterThanCpedReqValidation(reqDTO.getCustomIntervalPsd(), reqDTO.getCustomPed()))
        .linkWith(new PeriodReqValidation(reqDTO.getPeriods()))
        .linkWith(new PeriodLessThan12ReqValidation(reqDTO.getPeriods()))
        .linkWith(new PeriodContainYearToDateReqValidation(reqDTO.getPeriods()))
        .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingReqValidation(reqDTO.getHoldings()));
  }
}
