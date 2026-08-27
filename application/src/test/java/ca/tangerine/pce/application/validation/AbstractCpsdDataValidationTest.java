package ca.tangerine.pce.application.validation;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.application.returns.FxContext;
import ca.tangerine.pce.application.returns.ProcessingCase;
import ca.tangerine.pce.application.returns.ProcessingContext;
import ca.tangerine.pce.application.returns.ReturnsSnapshot;
import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.BasePceException;

abstract class AbstractCpsdDataValidationTest {

  protected abstract CpsdDataValidation validator();

  protected abstract ErrorCode expectedAfterPedCode();

  protected abstract ErrorCode expectedBeforePsdCode();

  protected abstract List<ProcessingCase> expectedApplicableCases();

  @Test
  void shouldReturnEmptyErrors_whenCpsdIsNull() {
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = snapshot(LocalDate.parse("2020-01-31"),
        LocalDate.parse("2024-12-31"));

    ReturnsSnapshot<HoldingMonthlyReturns> result = validator().process(snapshot,
        ProcessingContext.of(null, null, FxContext.empty()));

    assertThat(result.errors()).isEmpty();
  }

  @Test
  void shouldReturnAfterPedError_whenCpsdIsAfterPed() {
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = snapshot(LocalDate.parse("2020-01-31"),
        LocalDate.parse("2024-12-31"));

    ReturnsSnapshot<HoldingMonthlyReturns> result = validator().process(snapshot,
        ProcessingContext.of(LocalDate.parse("2025-12-31"), null, FxContext.empty()));

    assertThat(result.errors())
        .extracting(BasePceException::getErrorCode)
        .containsExactly(expectedAfterPedCode());
  }

  @Test
  void shouldReturnBeforePsdError_whenCpsdIsBeforePsd() {
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = snapshot(LocalDate.parse("2020-01-31"),
        LocalDate.parse("2024-12-31"));

    ReturnsSnapshot<HoldingMonthlyReturns> result = validator().process(snapshot,
        ProcessingContext.of(LocalDate.parse("2019-12-31"), null, FxContext.empty()));

    assertThat(result.errors())
        .extracting(BasePceException::getErrorCode)
        .containsExactly(expectedBeforePsdCode());
  }

  @Test
  void shouldReturnEmptyErrors_whenCpsdIsWithinWindow() {
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = snapshot(LocalDate.parse("2020-01-31"),
        LocalDate.parse("2024-12-31"));

    ReturnsSnapshot<HoldingMonthlyReturns> result = validator().process(snapshot,
        ProcessingContext.of(LocalDate.parse("2022-06-30"), null, FxContext.empty()));

    assertThat(result.errors()).isEmpty();
  }

  @Test
  void shouldApplyOnlyToConfiguredCases_whenIsApplicable() {
    List<ProcessingCase> applicable = expectedApplicableCases();
    for (ProcessingCase processingCase : ProcessingCase.values()) {
      assertThat(validator().isApplicable(processingCase))
          .as("case %s", processingCase)
          .isEqualTo(applicable.contains(processingCase));
    }
  }

  private static ReturnsSnapshot<HoldingMonthlyReturns> snapshot(LocalDate psd, LocalDate ped) {
    return new ReturnsSnapshot<>(Map.of(), Map.of(), psd, ped, List.of());
  }
}
