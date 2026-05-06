package com.fintex.ce.application.validation;

import com.fintex.ce.application.returns.ProcessingCase;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.EqualsAndHashCode;

import static com.fintex.ce.application.returns.ProcessingCase.BENCHMARK_PRE_PSD_TRIM;
import static com.fintex.ce.application.returns.ProcessingCase.BENCHMARK_WEIGHTED_AVERAGE_WITH_CPED_ONLY;
import static com.fintex.ce.application.returns.ProcessingCase.BENCHMARK_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED;
import static com.fintex.ce.model.error.ErrorCode.CPED_AFTER_BENCHMARK_PED;
import static com.fintex.ce.model.error.ErrorCode.CPED_BEFORE_BENCHMARK_PSD;

@Component
@Order(100)
@EqualsAndHashCode
public class BenchmarkCpedDataValidation extends CpedDataValidation {

  @Override
  protected ErrorCode getCpedIsAfterPedExceptionCode() {
    return CPED_AFTER_BENCHMARK_PED;
  }

  @Override
  protected ErrorCode getCpedIsBeforePsdExceptionCode() {
    return CPED_BEFORE_BENCHMARK_PSD;
  }

  @Override
  public boolean isApplicable(ProcessingCase processingCase) {
    return processingCase == BENCHMARK_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED
        || processingCase == BENCHMARK_WEIGHTED_AVERAGE_WITH_CPED_ONLY
        || processingCase == BENCHMARK_PRE_PSD_TRIM;
  }
}
