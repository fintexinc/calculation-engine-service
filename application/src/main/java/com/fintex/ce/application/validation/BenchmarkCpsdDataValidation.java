package com.fintex.ce.application.validation;

import com.fintex.ce.application.returns.ProcessingCase;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.EqualsAndHashCode;

import static com.fintex.ce.application.returns.ProcessingCase.BENCHMARK_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED;
import static com.fintex.ce.model.error.ErrorCode.CPSD_AFTER_BENCHMARK_PED;
import static com.fintex.ce.model.error.ErrorCode.CPSD_BEFORE_BENCHMARK_PSD;

@Component
@Order(110)
@EqualsAndHashCode
public class BenchmarkCpsdDataValidation extends CpsdDataValidation {

  @Override
  protected ErrorCode getCpsdIsBeforePsdExceptionCode() {
    return CPSD_BEFORE_BENCHMARK_PSD;
  }

  @Override
  protected ErrorCode getCpsdIsAfterPedExceptionCode() {
    return CPSD_AFTER_BENCHMARK_PED;
  }

  @Override
  public boolean isApplicable(ProcessingCase processingCase) {
    return processingCase == BENCHMARK_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED;
  }
}
