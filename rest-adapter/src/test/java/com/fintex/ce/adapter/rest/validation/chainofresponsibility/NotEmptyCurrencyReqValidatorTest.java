package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.exception.ReqValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.domain.enumeration.ExceptionCode.ERR_RRC_MC_001;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotEmptyCurrencyReqValidatorTest {

  @Test
  void check_periodIsLessThanZero() {
    // SETUP
    final var sut = new NotEmptyCurrencyReqValidator(null);

    final ReqValidationException expected = ERR_RRC_MC_001.reqValidationError();

    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void check_validCase() {
    // SETUP
    final var sut = new NotEmptyCurrencyReqValidator(Currency.CAD);

    // ACT
    Assertions.assertDoesNotThrow(sut::check);

    // VERIFY
  }

}