package com.fintex.ce.util.validation.request;

import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.util.validation.request.chainofresponsibility.CipsdGreaterThanCpedReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CipsdLastDayOfMonthReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpedLastDayOfMonthReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotEmptyGicInterestRateReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.PeriodReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.ReqValidation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.fintex.ce.config.enumeration.Currency.CAD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class TrailingTotalReturnsReqValidatorTest {

    @Test
    void build_checkResult() {
        //SETUP
        final var sut = new TrailingTotalReturnsReqValidator();

        final var reqDTO = getPeriodsReqDTO();

        final ReqValidation expected = ReqValidation.create()
                .linkWith(new NotNullReqValidation(reqDTO))
                .linkWith(new NotEmptyCurrencyReqValidator(reqDTO.getCurrency()))
                .linkWith(new CipsdLastDayOfMonthReqValidation(reqDTO.getCustomIntervalPsd()))
                .linkWith(new CpedLastDayOfMonthReqValidation(reqDTO.getCustomPed()))
                .linkWith(new CipsdGreaterThanCpedReqValidation(reqDTO.getCustomIntervalPsd(), reqDTO.getCustomPed()))
                .linkWith(new PeriodReqValidation(reqDTO.getPeriods()))
                .linkWith(new NotEmptyGicInterestRateReqValidator(reqDTO.getHoldings()))
                .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));

        //ACT
        final ReqValidation actual = sut.build(reqDTO);

        //VERIFY
        assertEquals(expected, actual);
    }

    static PeriodsReqDTO getPeriodsReqDTO() {
        final var reqDTO = new PeriodsReqDTO();
        reqDTO.setCurrency(CAD);
        reqDTO.setPeriods(Set.of("1", "2", "3"));
        reqDTO.setCustomIntervalPsd(LocalDate.of(2019, 5, 31));
        reqDTO.setCustomPed(LocalDate.of(2020, 5, 31));
        reqDTO.setHoldings(List.of(mock(Holding.class)));
        return reqDTO;
    }

}