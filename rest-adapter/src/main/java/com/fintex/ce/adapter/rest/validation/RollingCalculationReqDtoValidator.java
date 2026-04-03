package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpedLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpsdGreaterThanCpedReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpsdLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotIncludeCipsdReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodsNotContainingSincePerformanceStartDateReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodsNotContainingYearToDateReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.ReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.RollingPeriodsLessThan12ReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.RollingPeriodsReqValidation;
import com.fintex.ce.domain.dto.command.RollingCalculationCommand;
import org.springframework.stereotype.Component;

@Component
public class RollingCalculationReqDtoValidator extends AbstractRequestValidator<RollingCalculationCommand> {

  @Override
  public ReqValidation build(final RollingCalculationCommand reqDTO) {
    return ReqValidation.create()
        .linkWith(new NotNullReqValidation(reqDTO))
        .linkWith(new NotEmptyCurrencyReqValidator(reqDTO.getCurrency()))
        .linkWith(new CpsdLastDayOfMonthReqValidation(reqDTO.getCustomPsd()))
        .linkWith(new CpedLastDayOfMonthReqValidation(reqDTO.getCustomPed()))
        .linkWith(new CpsdGreaterThanCpedReqValidation(reqDTO.getCustomPsd(), reqDTO.getCustomPed()))
        .linkWith(new NotIncludeCipsdReqValidation(reqDTO.getCustomIntervalPsd()))
        .linkWith(new PeriodsNotContainingYearToDateReqValidation(reqDTO.getRollingPeriods()))
        .linkWith(new PeriodsNotContainingSincePerformanceStartDateReqValidation(reqDTO.getRollingPeriods()))
        .linkWith(new PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidation(reqDTO
            .getRollingPeriods()))
        .linkWith(new RollingPeriodsReqValidation(reqDTO.getRollingPeriods()))
        .linkWith(new RollingPeriodsLessThan12ReqValidation(reqDTO.getRollingPeriods()))
        .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));
  }

}
