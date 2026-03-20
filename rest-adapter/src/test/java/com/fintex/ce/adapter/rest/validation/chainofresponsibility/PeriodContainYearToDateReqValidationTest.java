package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.exception.ReqValidationException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_RRC_TIP_002;
import static com.fintex.ce.domain.model.enumeration.Period.YEAR_TO_DATE;
import static org.junit.jupiter.api.Assertions.*;

class PeriodContainYearToDateReqValidationTest {

  @Test
  void check_validCase1() {
    // SETUP
    final var sut = new PeriodContainYearToDateReqValidation(Set.of("10"));

    // ACT
    assertDoesNotThrow(sut::check);

    // VERIFY
  }

  @Test
  void check_validCase2() {
    // SETUP
    final var sut = new PeriodContainYearToDateReqValidation(Set.of());

    // ACT
    assertDoesNotThrow(sut::check);

    // VERIFY
  }

  @Test
  void check_periodsContainYearToDate() {
    // SETUP
    final var sut = new PeriodContainYearToDateReqValidation(Set.of(YEAR_TO_DATE.name()));

    final ReqValidationException expected = ERR_RRC_TIP_002.reqValidationError();

    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

    // VERIFY
    assertEquals(expected, actual);
  }

}