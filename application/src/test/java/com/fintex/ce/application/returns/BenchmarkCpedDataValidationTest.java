package com.fintex.ce.application.returns;

import com.fintex.ce.application.validation.BenchmarkCpedDataValidation;
import com.fintex.ce.model.error.ErrorCode;

import org.junit.jupiter.api.Test;

import static com.fintex.ce.model.error.ErrorCode.CPED_AFTER_BENCHMARK_PED;
import static com.fintex.ce.model.error.ErrorCode.CPED_BEFORE_BENCHMARK_PSD;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BenchmarkCpedDataValidationTest {

  @Test
  void shouldGetCpedIsAfterPedExceptionCode_whenCheckResult() {
    // SETUP
    final BenchmarkCpedDataValidation sut = new BenchmarkCpedDataValidation();

    // ACT
    final ErrorCode actual = sut.getCpedIsAfterPedExceptionCode();

    // VERIFY
    assertEquals(CPED_AFTER_BENCHMARK_PED, actual);
  }

  @Test
  void shouldGetCpedIsBeforePsdExceptionCode_whenCheckResult() {
    // SETUP
    final BenchmarkCpedDataValidation sut = new BenchmarkCpedDataValidation();

    // ACT
    final ErrorCode actual = sut.getCpedIsBeforePsdExceptionCode();

    // VERIFY
    assertEquals(CPED_BEFORE_BENCHMARK_PSD, actual);
  }

}