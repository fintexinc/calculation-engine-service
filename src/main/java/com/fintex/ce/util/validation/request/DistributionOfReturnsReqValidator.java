package com.fintex.ce.util.validation.request;

import com.fintex.ce.dto.request.DistributionOfReturnsReqDTO;
import com.fintex.ce.util.validation.request.chainofresponsibility.CipsdGreaterThanCpedReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CipsdLastDayOfMonthReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpedLastDayOfMonthReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpsdGreaterThanCpedReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpsdLastDayOfMonthReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CustomNumberOfBinsGreaterThan30DistributionOfReturnsReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CustomNumberOfBinsLessThan5DistributionOfReturnsReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.PeriodReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.ReqValidation;
import org.springframework.stereotype.Component;

@Component
public class DistributionOfReturnsReqValidator extends AbstractRequestValidator<DistributionOfReturnsReqDTO> {

    @Override
    public ReqValidation build(final DistributionOfReturnsReqDTO reqDTO) {
        return ReqValidation.create()
                .linkWith(new NotNullReqValidation(reqDTO))
                .linkWith(new NotEmptyCurrencyReqValidator(reqDTO.getCurrency()))
                .linkWith(new CpedLastDayOfMonthReqValidation(reqDTO.getCustomPed()))
                .linkWith(new CpsdLastDayOfMonthReqValidation(reqDTO.getCustomPsd()))
                .linkWith(new CipsdLastDayOfMonthReqValidation(reqDTO.getCustomIntervalPsd()))
                .linkWith(new CpsdGreaterThanCpedReqValidation(reqDTO.getCustomPsd(), reqDTO.getCustomPed()))
                .linkWith(new CipsdGreaterThanCpedReqValidation(reqDTO.getCustomIntervalPsd(), reqDTO.getCustomPed()))
                .linkWith(new CustomNumberOfBinsLessThan5DistributionOfReturnsReqValidation(reqDTO.getCustomNumberOfBins()))
                .linkWith(new CustomNumberOfBinsGreaterThan30DistributionOfReturnsReqValidation(reqDTO.getCustomNumberOfBins()))
                .linkWith(new PeriodReqValidation(reqDTO.getPeriods()))
                .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));
    }


}
