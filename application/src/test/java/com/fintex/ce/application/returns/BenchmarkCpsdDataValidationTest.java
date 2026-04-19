package com.fintex.ce.application.returns;

import com.fintex.ce.application.validation.BenchmarkCpsdDataValidation;
import com.fintex.ce.model.error.ErrorCode;

import org.junit.jupiter.api.Test;

import static com.fintex.ce.model.error.ErrorCode.CPSD_AFTER_BENCHMARK_PED;
import static com.fintex.ce.model.error.ErrorCode.CPSD_BEFORE_BENCHMARK_PSD;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BenchmarkCpsdDataValidationTest {

  @Test
  void shouldGetCpsdIsBeforePsdExceptionCode_whenCheckResult() {
    // SETUP
    final BenchmarkCpsdDataValidation sut = new BenchmarkCpsdDataValidation();

    // ACT
    final ErrorCode actual = sut.getCpsdIsBeforePsdExceptionCode();

    // VERIFY
    assertEquals(CPSD_BEFORE_BENCHMARK_PSD, actual);
  }

  @Test
  void shouldGetCpsdIsAfterPedExceptionCode_whenCheckResult() {
    // SETUP
    final BenchmarkCpsdDataValidation sut = new BenchmarkCpsdDataValidation();

    // ACT
    final ErrorCode actual = sut.getCpsdIsAfterPedExceptionCode();

    // VERIFY
    assertEquals(CPSD_AFTER_BENCHMARK_PED, actual);
  }

}