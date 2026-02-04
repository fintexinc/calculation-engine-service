package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.exception.ReqValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.fintex.ce.domain.enumeration.ExceptionCode.ERR_RRC_CPED_001;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpedLastDayOfMonthReqValidationTest {

  @Test
  void check_cpedIsNotLastDayOfMonth() {
    // SETUP
    final var sut = new CpedLastDayOfMonthReqValidation(LocalDate.of(2000, 5, 5));

    final ReqValidationException expected = ERR_RRC_CPED_001.reqValidationError();

    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void check_validCase() {
    // SETUP
    final var sut = new CpedLastDayOfMonthReqValidation(LocalDate.of(2000, 5, 31));

    // ACT
    sut.check();

    // VERIFY
  }

}