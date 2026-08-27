package ca.tangerine.pce.application.validation;

import java.util.List;

import ca.tangerine.pce.application.returns.ProcessingCase;
import ca.tangerine.pce.model.error.ErrorCode;

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
