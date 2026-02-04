package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.exception.ReqValidationException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.fintex.ce.domain.enumeration.ExceptionCode.ERR_RRC_RTIP_001;
import static org.junit.jupiter.api.Assertions.*;

class RollingPeriodsLessThan12ReqValidationTest {

  @Test
  void check_periodLessThan12() {
    // SETUP
    final var sut = new RollingPeriodsLessThan12ReqValidation(
        Set.of("1", "2"));

    final ReqValidationException expected = ERR_RRC_RTIP_001.reqValidationError();

    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void check_validCase() {
    // SETUP
    final var sut = new RollingPeriodsLessThan12ReqValidation(Set.of());

    // ACT
    assertDoesNotThrow(sut::check);

    // VERIFY
  }

}