package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.TopCommonHoldingsReqDTO;
import com.fintex.ce.exception.ReqValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.fintex.ce.config.enumeration.ExceptionCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class TopCommonHoldingsReqValidationTest {

    @Test
    void check_numOfFundsMinLessThanOne() {
        //SETUP
        final TopCommonHoldingsReqDTO reqDTO = new TopCommonHoldingsReqDTO();
        reqDTO.setNumOfFundsMin(0);

        final var sut = new TopCommonHoldingsReqValidation(reqDTO);

        final ReqValidationException expected = ERR_TCH_NFM_001.reqValidationError();

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void check_numOfFundsMinGreaterThanHoldingSize() {
        //SETUP
        final TopCommonHoldingsReqDTO reqDTO = new TopCommonHoldingsReqDTO();
        reqDTO.setNumOfFundsMin(3);
        reqDTO.setHoldings(List.of(mock(Holding.class), mock(Holding.class)));

        final var sut = new TopCommonHoldingsReqValidation(reqDTO);

        final ReqValidationException expected = ERR_TCH_NFM_002.reqValidationError();

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void check_sizeOfAccumulateHoldingTypesGreaterThan12() {
        //SETUP
        final TopCommonHoldingsReqDTO reqDTO = new TopCommonHoldingsReqDTO();
        reqDTO.setAccumulateHoldingTypes(Set.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"));
        reqDTO.setHoldings(List.of(mock(Holding.class), mock(Holding.class)));

        final var sut = new TopCommonHoldingsReqValidation(reqDTO);

        final ReqValidationException expected = ERR_TCH_AHT_001.reqValidationError();

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
    }

}