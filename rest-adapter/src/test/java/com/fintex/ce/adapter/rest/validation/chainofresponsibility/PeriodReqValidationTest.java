package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.exception.ReqValidationException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_RRC_TIP_003;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_RRC_TIP_004;
import static com.fintex.ce.domain.model.enumeration.Period.YEAR_TO_DATE;
import static org.junit.jupiter.api.Assertions.*;

class PeriodReqValidationTest {

  @Test
  void check_periodsContainPeriodThatIsLessThanZero() {
    // SETUP
    final var sut = new PeriodReqValidation(Set.of("-1"));

    final ReqValidationException expected = ERR_RRC_TIP_003.reqValidationError();

    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void check_periodsContainNotAllowedPeriod() {
    // SETUP
    final var sut = new PeriodReqValidation(Set.of("NotAllowedSymbol"));

    final ReqValidationException expected = ERR_RRC_TIP_004.reqValidationError();

    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void check_validCase1() {
    // SETUP
    final var sut = new PeriodReqValidation(Set.of());

    // ACT
    assertDoesNotThrow(sut::check);

    // VERIFY
  }

  @Test
  void check_validCase2() {
    // SETUP
    final var sut = new PeriodReqValidation(Set.of(YEAR_TO_DATE.name()));

    // ACT
    assertDoesNotThrow(sut::check);

    // VERIFY
  }

}