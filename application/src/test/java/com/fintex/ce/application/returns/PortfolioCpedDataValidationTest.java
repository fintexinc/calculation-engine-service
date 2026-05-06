package com.fintex.ce.application.returns;

import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.model.error.ErrorCode;

import org.junit.jupiter.api.Test;

import static com.fintex.ce.model.error.ErrorCode.CPED_AFTER_PORTFOLIO_PED;
import static com.fintex.ce.model.error.ErrorCode.CPED_BEFORE_PORTFOLIO_PSD;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PortfolioCpedDataValidationTest {

  @Test
  void shouldGetCpedIsAfterPedExceptionCode_whenCheckResult() {
    // SETUP
    final PortfolioCpedDataValidation validation = new PortfolioCpedDataValidation();

    // ACT
    final ErrorCode actual = validation.getCpedIsAfterPedExceptionCode();

    // VERIFY
    assertEquals(CPED_AFTER_PORTFOLIO_PED, actual);
  }

  @Test
  void shouldGetCpedIsBeforePsdExceptionCode_whenCheckResult() {
    // SETUP
    final PortfolioCpedDataValidation validation = new PortfolioCpedDataValidation();

    // ACT
    final ErrorCode actual = validation.getCpedIsBeforePsdExceptionCode();

    // VERIFY
    assertEquals(CPED_BEFORE_PORTFOLIO_PSD, actual);
  }

}