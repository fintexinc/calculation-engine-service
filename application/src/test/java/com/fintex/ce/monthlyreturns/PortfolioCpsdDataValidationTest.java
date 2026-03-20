package com.fintex.ce.monthlyreturns;

import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_RRC_CPSD_002;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_RRC_CPSD_003;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PortfolioCpsdDataValidationTest {

  @Test
  void shouldGetCpsdIsBeforePsdExceptionCode_whenCheckResult() {
    // SETUP
    final PortfolioCpsdDataValidation sut = new PortfolioCpsdDataValidation();

    // ACT
    final ExceptionCode actual = sut.getCpsdIsBeforePsdExceptionCode();

    // VERIFY
    assertEquals(ERR_RRC_CPSD_002, actual);
  }

  @Test
  void shouldGetCpsdIsAfterPedExceptionCode_whenCheckResult() {
    // SETUP
    final PortfolioCpsdDataValidation sut = new PortfolioCpsdDataValidation();

    // ACT
    final ExceptionCode actual = sut.getCpsdIsAfterPedExceptionCode();

    // VERIFY
    assertEquals(ERR_RRC_CPSD_003, actual);
  }

}