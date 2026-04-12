package com.fintex.ce.application.returns;

import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.domain.exception.code.ErrorCode;

import org.junit.jupiter.api.Test;

import static com.fintex.ce.domain.exception.code.ErrorCode.ERR_RRC_CPSD_002;
import static com.fintex.ce.domain.exception.code.ErrorCode.ERR_RRC_CPSD_003;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PortfolioCpsdDataValidationTest {

  @Test
  void shouldGetCpsdIsBeforePsdExceptionCode_whenCheckResult() {
    // SETUP
    final PortfolioCpsdDataValidation sut = new PortfolioCpsdDataValidation();

    // ACT
    final ErrorCode actual = sut.getCpsdIsBeforePsdExceptionCode();

    // VERIFY
    assertEquals(ERR_RRC_CPSD_002, actual);
  }

  @Test
  void shouldGetCpsdIsAfterPedExceptionCode_whenCheckResult() {
    // SETUP
    final PortfolioCpsdDataValidation sut = new PortfolioCpsdDataValidation();

    // ACT
    final ErrorCode actual = sut.getCpsdIsAfterPedExceptionCode();

    // VERIFY
    assertEquals(ERR_RRC_CPSD_003, actual);
  }

}