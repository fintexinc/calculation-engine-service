package com.fintex.ce.util.validation.request;

import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.RollingCalculationReqDTO;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpedLastDayOfMonthReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpsdGreaterThanCpedReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpsdLastDayOfMonthReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotIncludeCipsdReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.PeriodsNotContainingSincePerformanceStartDateReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.PeriodsNotContainingYearToDateReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.ReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.RollingPeriodsLessThan12ReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.RollingPeriodsReqValidation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.fintex.ce.config.enumeration.Currency.CAD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RollingCalculationReqDtoValidatorTest {

    @Test
    void build_checkResult() {
        //SETUP
        final var sut = new RollingCalculationReqDtoValidator();

        final RollingCalculationReqDTO reqDTO = getRollingCalculationReqDTO();

        final ReqValidation expected = ReqValidation.create()
                .linkWith(new NotNullReqValidation(reqDTO))
                .linkWith(new NotEmptyCurrencyReqValidator(reqDTO.getCurrency()))
                .linkWith(new CpsdLastDayOfMonthReqValidation(reqDTO.getCustomPsd()))
                .linkWith(new CpedLastDayOfMonthReqValidation(reqDTO.getCustomPed()))
                .linkWith(new CpsdGreaterThanCpedReqValidation(reqDTO.getCustomPsd(), reqDTO.getCustomPed()))
                .linkWith(new NotIncludeCipsdReqValidation(reqDTO.getCustomIntervalPsd()))
                .linkWith(new PeriodsNotContainingYearToDateReqValidation(reqDTO.getRollingPeriods()))
                .linkWith(new PeriodsNotContainingSincePerformanceStartDateReqValidation(reqDTO.getRollingPeriods()))
                .linkWith(new PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidation(reqDTO.getRollingPeriods()))
                .linkWith(new RollingPeriodsReqValidation(reqDTO.getRollingPeriods()))
                .linkWith(new RollingPeriodsLessThan12ReqValidation(reqDTO.getRollingPeriods()))
                .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));

        //ACT
        final ReqValidation actual = sut.build(reqDTO);

        //VERIFY
        assertEquals(expected, actual);
    }

    static RollingCalculationReqDTO getRollingCalculationReqDTO() {
        final var reqDTO = new RollingCalculationReqDTO();
        reqDTO.setCurrency(CAD);
        reqDTO.setCustomPsd(LocalDate.now());
        reqDTO.setCustomPed(LocalDate.now().plusMonths(1));
        reqDTO.setCustomIntervalPsd(LocalDate.now().minusMonths(1));
        reqDTO.setRollingPeriods(Set.of("1", "2", "3"));
        reqDTO.setHoldings(List.of(mock(Holding.class)));
        reqDTO.setBenchmarkHoldings(List.of(mock(Holding.class), mock(Holding.class)));
        return reqDTO;
    }

}