package com.fintex.ce.application.returns;

import com.fintex.ce.application.validation.BenchmarkCpedDataValidation;
import com.fintex.ce.domain.exception.code.ErrorCode;

import org.junit.jupiter.api.Test;

import static com.fintex.ce.domain.exception.code.ErrorCode.ERR_RRC_BMPED_002;
import static com.fintex.ce.domain.exception.code.ErrorCode.ERR_RRC_BMPED_003;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BenchmarkCpedDataValidationTest {

  @Test
  void shouldGetCpedIsAfterPedExceptionCode_whenCheckResult() {
    // SETUP
    final BenchmarkCpedDataValidation sut = new BenchmarkCpedDataValidation();

    // ACT
    final ErrorCode actual = sut.getCpedIsAfterPedExceptionCode();

    // VERIFY
    assertEquals(ERR_RRC_BMPED_003, actual);
  }

  @Test
  void shouldGetCpedIsBeforePsdExceptionCode_whenCheckResult() {
    // SETUP
    final BenchmarkCpedDataValidation sut = new BenchmarkCpedDataValidation();

    // ACT
    final ErrorCode actual = sut.getCpedIsBeforePsdExceptionCode();

    // VERIFY
    assertEquals(ERR_RRC_BMPED_002, actual);
  }

}