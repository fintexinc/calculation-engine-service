package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.adapter.rest.dto.request.ReturnReqDTO;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpedLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpsdGreaterThanCpedReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpsdLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.ReqValidation;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ReturnReqDtoValidatorTest {

  @Test
  void build_checkResult() {
    final var sut = new ReturnReqDtoValidator();

    final var reqDTO = new ReturnReqDTO();
    reqDTO.setCurrency(CurrencyType.CAD);
    reqDTO.setCustomPerformanceStartDate(LocalDate.now().plusMonths(1));
    reqDTO.setCustomPerformanceEndDate(LocalDate.now().minusMonths(1));
    reqDTO.setHoldings(List.of(mock(Holding.class)));

    final ReqValidation expected = ReqValidation.create()
        .linkWith(new NotEmptyCurrencyReqValidator(reqDTO.getCurrency()))
        .linkWith(new CpsdLastDayOfMonthReqValidation(reqDTO.getCustomPerformanceStartDate()))
        .linkWith(new CpedLastDayOfMonthReqValidation(reqDTO.getCustomPerformanceEndDate()))
        .linkWith(new CpsdGreaterThanCpedReqValidation(reqDTO.getCustomPerformanceStartDate(), reqDTO
            .getCustomPerformanceEndDate()))
        .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));

    final ReqValidation actual = sut.build(reqDTO);

    assertEquals(expected, actual);
  }

}