package com.fintex.ce.application.validation;

import com.fintex.ce.application.returns.ProcessingCase;
import com.fintex.ce.model.error.ErrorCode;

import java.util.List;

class BenchmarkCpedDataValidationTest extends AbstractCpedDataValidationTest {

  private final BenchmarkCpedDataValidation validation = new BenchmarkCpedDataValidation();

  @Override
  protected CpedDataValidation validator() {
    return validation;
  }

  @Override
  protected ErrorCode expectedAfterPedCode() {
    return ErrorCode.CPED_AFTER_BENCHMARK_PED;
  }

  @Override
  protected ErrorCode expectedBeforePsdCode() {
    return ErrorCode.CPED_BEFORE_BENCHMARK_PSD;
  }

  @Override
  protected List<ProcessingCase> expectedApplicableCases() {
    return List.of(
        ProcessingCase.BENCHMARK_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED,
        ProcessingCase.BENCHMARK_WEIGHTED_AVERAGE_WITH_CPED_ONLY,
        ProcessingCase.BENCHMARK_PRE_PSD_TRIM);
  }
}
