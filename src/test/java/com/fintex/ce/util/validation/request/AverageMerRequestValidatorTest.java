package com.fintex.ce.util.validation.request;

import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.AverageMerRequestDTO;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.ReqValidation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AverageMerRequestValidatorTest {


    @Test
    void build_checkResult() {
        //SETUP
        final var sut = new AverageMerRequestValidator();

        final AverageMerRequestDTO reqDTO = new AverageMerRequestDTO();
        reqDTO.setHoldings(List.of(mock(Holding.class)));

        final ReqValidation expected = ReqValidation.create()
                .linkWith(new NotNullReqValidation(reqDTO))
                .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));

        //ACT
        final ReqValidation actual = sut.build(reqDTO);

        //VERIFY
        assertEquals(expected, actual);
    }


}