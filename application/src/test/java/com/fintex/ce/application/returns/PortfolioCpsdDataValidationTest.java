package com.fintex.ce.application.returns;

import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.model.error.ErrorCode;

import org.junit.jupiter.api.Test;

import static com.fintex.ce.model.error.ErrorCode.CPSD_AFTER_PORTFOLIO_PED;
import static com.fintex.ce.model.error.ErrorCode.CPSD_BEFORE_PORTFOLIO_PSD;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PortfolioCpsdDataValidationTest {

  @Test
  void shouldGetCpsdIsBeforePsdExceptionCode_whenCheckResult() {
    // SETUP
    final PortfolioCpsdDataValidation sut = new PortfolioCpsdDataValidation();

    // ACT
    final ErrorCode actual = sut.getCpsdIsBeforePsdExceptionCode();

    // VERIFY
    assertEquals(CPSD_BEFORE_PORTFOLIO_PSD, actual);
  }

  @Test
  void shouldGetCpsdIsAfterPedExceptionCode_whenCheckResult() {
    // SETUP
    final PortfolioCpsdDataValidation sut = new PortfolioCpsdDataValidation();

    // ACT
    final ErrorCode actual = sut.getCpsdIsAfterPedExceptionCode();

    // VERIFY
    assertEquals(CPSD_AFTER_PORTFOLIO_PED, actual);
  }

}