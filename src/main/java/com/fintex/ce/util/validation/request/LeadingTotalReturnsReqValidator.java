package com.fintex.ce.util.validation.request;

import com.fintex.ce.dto.request.LeadingTotalReturnPeriodsReqDTO;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpsdLastDayOfMonthReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotIncludeCipsdReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotIncludeCpedReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.PeriodReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.PeriodsNotContainingSincePerformanceStartDateReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.PeriodsNotContainingYearToDateReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.ReqValidation;
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
