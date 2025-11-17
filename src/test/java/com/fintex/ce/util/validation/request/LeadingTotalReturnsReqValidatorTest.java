package com.fintex.ce.util.validation.request;

import com.fintex.ce.dto.holding.Holding;
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
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.fintex.ce.config.enumeration.Currency.CAD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class LeadingTotalReturnsReqValidatorTest {

    @Test
    void build_checkResult() {
        //SETUP
        final var sut = new LeadingTotalReturnsReqValidator();

        final var reqDTO = getPeriodsReqDTO();

        final ReqValidation expected = ReqValidation.create()
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
        //ACT
        final ReqValidation actual = sut.build(reqDTO);

        //VERIFY
        assertEquals(expected, actual);
    }

    static LeadingTotalReturnPeriodsReqDTO getPeriodsReqDTO() {
        final var reqDTO = new LeadingTotalReturnPeriodsReqDTO();
        reqDTO.setCurrency(CAD);
        reqDTO.setPeriods(Set.of("1", "2", "3"));
        reqDTO.setCustomIntervalPsd(LocalDate.of(2019, 5, 31));
        reqDTO.setCustomPed(LocalDate.of(2020, 5, 31));
        reqDTO.setCustomPsd(LocalDate.of(2000, 3, 31));
        reqDTO.setHoldings(List.of(mock(Holding.class)));
        return reqDTO;
    }

}