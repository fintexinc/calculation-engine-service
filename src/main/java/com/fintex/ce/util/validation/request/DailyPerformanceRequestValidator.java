package com.fintex.ce.util.validation.request;

import com.fintex.ce.dto.calculation.HoldingForDailyCalculationDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.DailyPerformanceReqDTO;
import com.fintex.ce.util.validation.request.chainofresponsibility.CipsdGreaterThanCpedReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.ReqValidation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DailyPerformanceRequestValidator extends AbstractRequestValidator<DailyPerformanceReqDTO> {

    @Override
    public ReqValidation build(final DailyPerformanceReqDTO reqDTO) {
        return ReqValidation.create()
                .linkWith(new NotNullReqValidation(reqDTO))
                .linkWith(new CipsdGreaterThanCpedReqValidation(reqDTO.getStartDate(), reqDTO.getEndDate()))
                .linkWith(new HoldingsCouldNotBeEmptyReqValidation(getListOfHoldings(reqDTO)))
                .linkWith(new HoldingReqValidation(getListOfHoldings(reqDTO)));
    }

    private List<Holding> getListOfHoldings(final DailyPerformanceReqDTO reqDTO) {
        return reqDTO.getDailyHoldings().stream().map(HoldingForDailyCalculationDTO::getHolding).collect(Collectors.toList());
    }

}
