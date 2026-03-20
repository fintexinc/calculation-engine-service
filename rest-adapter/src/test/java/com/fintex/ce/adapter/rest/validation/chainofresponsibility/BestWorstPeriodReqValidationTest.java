package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.exception.ReqValidationException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_BWP_BWPTIP_001;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_BWP_BWPTIP_002;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BestWorstPeriodReqValidationTest {

  @Test
  void check_periodsIsEmpty_doNothing() {
    // SETUP
    final var sut = new BestWorstPeriodReqValidation(Set.of());

    // ACT
    sut.check();

    // VERIFY
  }

  @Test
  void check_periodIsLessThanZero() {
    // SETUP
    final var sut = new BestWorstPeriodReqValidation(Set.of(-1L, 10L));

    final ReqValidationException expected = ERR_BWP_BWPTIP_001.reqValidationError();

    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void check_periodIsGreaterThan300() {
    // SETUP
    final var sut = new BestWorstPeriodReqValidation(Set.of(301L, 10L));

    final ReqValidationException expected = ERR_BWP_BWPTIP_002.reqValidationError();

    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void check_validCase() {
    // SETUP
    final var sut = new BestWorstPeriodReqValidation(Set.of(299L, 10L, 100L));

    // ACT
    sut.check();

    // VERIFY
  }
}
