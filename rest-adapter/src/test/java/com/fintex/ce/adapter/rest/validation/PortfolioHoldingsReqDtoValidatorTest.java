package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.adapter.rest.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotEmptyGicTermReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.ReqValidation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class PortfolioHoldingsReqDtoValidatorTest {

  @Test
  void build_checkResult() {
    // SETUP
    final var sut = new PortfolioHoldingsReqDtoValidator();

    final var reqDTO = new PortfolioHoldingsReqDTO();
    reqDTO.setHoldings(List.of(mock(Holding.class)));

    final ReqValidation expected = ReqValidation.create()
        .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
        .linkWith(new NotEmptyGicTermReqValidator(reqDTO.getHoldings()))
        .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));

    // ACT
    final ReqValidation actual = sut.build(reqDTO);

    // VERIFY
    assertEquals(expected, actual);
  }

}
