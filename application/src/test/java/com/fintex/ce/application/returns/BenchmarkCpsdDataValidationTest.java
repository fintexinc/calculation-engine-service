package com.fintex.ce.application.returns;

import com.fintex.ce.application.validation.BenchmarkCpsdDataValidation;
import com.fintex.ce.domain.exception.code.ErrorCode;

import org.junit.jupiter.api.Test;

import static com.fintex.ce.domain.exception.code.ErrorCode.ERR_RRC_BMPSD_002;
import static com.fintex.ce.domain.exception.code.ErrorCode.ERR_RRC_BMPSD_003;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BenchmarkCpsdDataValidationTest {

  @Test
  void shouldGetCpsdIsBeforePsdExceptionCode_whenCheckResult() {
    // SETUP
    final BenchmarkCpsdDataValidation sut = new BenchmarkCpsdDataValidation();

    // ACT
    final ErrorCode actual = sut.getCpsdIsBeforePsdExceptionCode();

    // VERIFY
    assertEquals(ERR_RRC_BMPSD_002, actual);
  }

  @Test
  void shouldGetCpsdIsAfterPedExceptionCode_whenCheckResult() {
    // SETUP
    final BenchmarkCpsdDataValidation sut = new BenchmarkCpsdDataValidation();

    // ACT
    final ErrorCode actual = sut.getCpsdIsAfterPedExceptionCode();

    // VERIFY
    assertEquals(ERR_RRC_BMPSD_003, actual);
  }

}