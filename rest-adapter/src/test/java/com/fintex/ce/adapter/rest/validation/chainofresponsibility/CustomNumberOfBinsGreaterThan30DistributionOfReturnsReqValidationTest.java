package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.exception.ReqValidationException;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_RRC_CNOB_002;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomNumberOfBinsGreaterThan30DistributionOfReturnsReqValidationTest {

  @Test
  void check_customNumberOfBinsGreaterThan30() {
    // SETUP
    final var sut = new CustomNumberOfBinsGreaterThan30DistributionOfReturnsReqValidation(40);

    final ReqValidationException expected = ERR_RRC_CNOB_002.reqValidationError();

    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void check_validCase() {
    // SETUP
    final var sut = new CustomNumberOfBinsGreaterThan30DistributionOfReturnsReqValidation(25);

    // ACT
    sut.check();

    // VERIFY
  }

}