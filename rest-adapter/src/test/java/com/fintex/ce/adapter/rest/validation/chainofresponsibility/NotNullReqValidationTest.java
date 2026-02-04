package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import org.junit.jupiter.api.Test;

import static com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotNullReqValidation.REQUEST_COULD_NOT_BE_NULL;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotNullReqValidationTest {
  @Test
  void check_throwException() {
    // SETUP
    final var sut = new NotNullReqValidation(null);

    final NullPointerException expected = new NullPointerException(REQUEST_COULD_NOT_BE_NULL);

    // ACT
    assertThrows(NullPointerException.class, () -> sut.check());

    // VERIFY
  }

  @Test
  void check_validCase() {
    // SETUP
    final var sut = new NotNullReqValidation(new Object());

    // ACT
    assertDoesNotThrow(() -> sut.check());

    // VERIFY
  }

}