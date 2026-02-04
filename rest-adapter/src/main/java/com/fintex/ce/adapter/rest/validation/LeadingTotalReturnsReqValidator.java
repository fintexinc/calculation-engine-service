package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.adapter.rest.dto.request.LeadingTotalReturnPeriodsReqDTO;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpsdLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotIncludeCipsdReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotIncludeCpedReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodsNotContainingSincePerformanceStartDateReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodsNotContainingYearToDateReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.ReqValidation;
import org.springframework.stereotype.Component;

@Component
public class LeadingTotalReturnsReqValidator extends AbstractRequestValidator<LeadingTotalReturnPeriodsReqDTO> {

  @Override
  public ReqValidation build(final LeadingTotalReturnPeriodsReqDTO reqDTO) {
    return ReqValidation.create()
        .linkWith(new NotNullReqValidation(reqDTO))
        .linkWith(new NotEmptyCurrencyReqValidator(reqDTO.getCurrency()))
        .linkWith(new NotIncludeCipsdReqValidation(reqDTO.getCustomIntervalPsd()))
        .linkWith(new NotIncludeCpedReqValidation(reqDTO.getCustomPed()))
        .linkWith(new CpsdLastDayOfMonthReqValidation(reqDTO.getCustomPsd()))
        .linkWith(new PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidation(reqDTO.getPeriods()))
        .linkWith(new PeriodsNotContainingSincePerformanceStartDateReqValidation(reqDTO.getPeriods()))
        .linkWith(new PeriodsNotContainingYearToDateReqValidation(reqDTO.getPeriods()))
        .linkWith(new PeriodReqValidation(reqDTO.getPeriods()))
        .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));
  }
}
