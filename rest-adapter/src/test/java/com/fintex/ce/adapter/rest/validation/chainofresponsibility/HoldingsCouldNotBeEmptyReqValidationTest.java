package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.exception.ReqValidationException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HoldingsCouldNotBeEmptyReqValidationTest {

  @Test
  void check_holdingsIsNull() {
    // SETUP
    final var sut = new HoldingsCouldNotBeEmptyReqValidation(null);

    final var expected = new ReqValidationException("Holdings could not be empty");

    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

    // VERIFY
    assertEquals(expected, actual);
  }

}