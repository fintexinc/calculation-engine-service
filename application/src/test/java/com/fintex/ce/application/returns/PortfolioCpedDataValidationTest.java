package com.fintex.ce.application.returns;

import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.domain.exception.code.ErrorCode;

import org.junit.jupiter.api.Test;

import static com.fintex.ce.domain.exception.code.ErrorCode.ERR_RRC_CPED_002;
import static com.fintex.ce.domain.exception.code.ErrorCode.ERR_RRC_CPED_003;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PortfolioCpedDataValidationTest {

  @Test
  void shouldGetCpedIsAfterPedExceptionCode_whenCheckResult() {
    // SETUP
    final PortfolioCpedDataValidation sut = new PortfolioCpedDataValidation();

    // ACT
    final ErrorCode actual = sut.getCpedIsAfterPedExceptionCode();

    // VERIFY
    assertEquals(ERR_RRC_CPED_003, actual);
  }

  @Test
  void shouldGetCpedIsBeforePsdExceptionCode_whenCheckResult() {
    // SETUP
    final PortfolioCpedDataValidation sut = new PortfolioCpedDataValidation();

    // ACT
    final ErrorCode actual = sut.getCpedIsBeforePsdExceptionCode();

    // VERIFY
    assertEquals(ERR_RRC_CPED_002, actual);
  }

}