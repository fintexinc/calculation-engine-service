package com.fintex.ce.application.validation;

import com.fintex.ce.model.error.ErrorCode;

import lombok.EqualsAndHashCode;

import static com.fintex.ce.model.error.ErrorCode.CPED_AFTER_BENCHMARK_PED;
import static com.fintex.ce.model.error.ErrorCode.CPED_BEFORE_BENCHMARK_PSD;

@EqualsAndHashCode
public class BenchmarkCpedDataValidation extends CpedDataValidation {

  @Override
  public ErrorCode getCpedIsAfterPedExceptionCode() {
    return CPED_AFTER_BENCHMARK_PED;
  }

  @Override
  public ErrorCode getCpedIsBeforePsdExceptionCode() {
    return CPED_BEFORE_BENCHMARK_PSD;
  }
}
