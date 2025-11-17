package com.fintex.ce.util.validation.request;

import com.fintex.ce.dto.holding.CashHolding;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotEmptyGicTermReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotNullCashCurrencyValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.ReqValidation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassificationAllocationReqValidatorTest {

    @Test
    void build_checkResult() {
        //SETUP
        final var sut = new ClassificationAllocationReqValidator();

        final PortfolioHoldingsReqDTO reqDTO = new PortfolioHoldingsReqDTO();
        reqDTO.setHoldings(List.of(new CashHolding()));

        final ReqValidation expected = ReqValidation.create()
                .linkWith(new NotNullReqValidation(reqDTO))
                .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
                .linkWith(new NotNullCashCurrencyValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
                .linkWith(new NotEmptyGicTermReqValidator(reqDTO.getHoldings()))
                .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));

        //ACT
        final ReqValidation actual = sut.build(reqDTO);

        //VERIFY
        assertEquals(expected, actual);
    }

}
