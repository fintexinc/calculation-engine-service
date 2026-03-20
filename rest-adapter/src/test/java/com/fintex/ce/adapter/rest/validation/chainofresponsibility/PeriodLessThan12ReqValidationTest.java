package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.exception.ReqValidationException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_RRC_TIP_001;
import static org.junit.jupiter.api.Assertions.*;

class PeriodLessThan12ReqValidationTest {

  @Test
  void check_periodsContainPeriodThatIsLessThan12() {
    // SETUP
    final var sut = new PeriodLessThan12ReqValidation(Set.of("11"));

    final ReqValidationException expected = ERR_RRC_TIP_001.reqValidationError();

    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void check_validCase1() {
    // SETUP
    final var sut = new PeriodLessThan12ReqValidation(Set.of("str"));

    // ACT
    assertDoesNotThrow(sut::check);
    // VERIFY
  }

  @Test
  void check_validCase2() {
    // SETUP
    final var sut = new PeriodLessThan12ReqValidation(Set.of());

    // ACT
    assertDoesNotThrow(sut::check);

    // VERIFY
  }

}