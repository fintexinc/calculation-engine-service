package ca.tangerine.pce.application.validation;

import ca.tangerine.pce.application.returns.ProcessingCase;
import ca.tangerine.pce.model.error.ErrorCode;

import java.util.List;

class BenchmarkCpsdDataValidationTest extends AbstractCpsdDataValidationTest {

  private final BenchmarkCpsdDataValidation validation = new BenchmarkCpsdDataValidation();

  @Override
  protected CpsdDataValidation validator() {
    return validation;
  }

  @Override
  protected ErrorCode expectedAfterPedCode() {
    return ErrorCode.CPSD_AFTER_BENCHMARK_PED;
  }

  @Override
  protected ErrorCode expectedBeforePsdCode() {
    return ErrorCode.CPSD_BEFORE_BENCHMARK_PSD;
  }

  @Override
  protected List<ProcessingCase> expectedApplicableCases() {
    return List.of(ProcessingCase.BENCHMARK_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED);
  }
}
