package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.sm.model.domain.enumeration.CurrencyType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_RRC_MC_001;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotEmptyCurrencyReqValidatorTest {

  @Test
  void check_periodIsLessThanZero() {
    final var sut = new NotEmptyCurrencyReqValidator(null);

    final ReqValidationException expected = ERR_RRC_MC_001.reqValidationError();

    final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

    assertEquals(expected, actual);
  }

  @Test
  void check_validCase() {
    final var sut = new NotEmptyCurrencyReqValidator(CurrencyType.CAD);

    Assertions.assertDoesNotThrow(sut::check);

  }

}