package com.fintex.ce.application.validation;

import com.fintex.ce.model.error.ErrorCode;

import lombok.EqualsAndHashCode;

import static com.fintex.ce.model.error.ErrorCode.CPSD_AFTER_BENCHMARK_PED;
import static com.fintex.ce.model.error.ErrorCode.CPSD_BEFORE_BENCHMARK_PSD;

@EqualsAndHashCode
public class BenchmarkCpsdDataValidation extends CpsdDataValidation {

  @Override
  public ErrorCode getCpsdIsBeforePsdExceptionCode() {
    return CPSD_BEFORE_BENCHMARK_PSD;
  }

  @Override
  public ErrorCode getCpsdIsAfterPedExceptionCode() {
    return CPSD_AFTER_BENCHMARK_PED;
  }
}
