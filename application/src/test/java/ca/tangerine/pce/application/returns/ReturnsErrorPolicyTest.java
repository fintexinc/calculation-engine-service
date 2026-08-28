package ca.tangerine.pce.application.returns;

import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.BasePceException;
import ca.tangerine.pce.model.error.exceptions.CalculationsFailedException;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReturnsErrorPolicyTest {

  @Test
  void shouldReturnSnapshot_whenThrowIfFatalAndNoErrors() {
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = ReturnsSnapshot.empty();

    ReturnsSnapshot<HoldingMonthlyReturns> result = ReturnsErrorPolicy.throwIfFatal(snapshot);

    assertThat(result).isSameAs(snapshot);
  }

  @Test
  void shouldReturnSnapshot_whenAllErrorsAreAllowed() {
    List<BasePceException> allowed = List.of(
        ErrorCode.HOLDING_PSD_OUT_OF_RANGE.toExceptionForId("h1"),
        ErrorCode.MISSING_MONTHLY_RETURNS.toExceptionForId("h2"),
        ErrorCode.HOLDING_MISSING_CURRENCY_FROM_MIC.toExceptionForId("h3"),
        ErrorCode.CPED_AFTER_PORTFOLIO_PED.toExceptionForId("h4"));
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = ReturnsSnapshot.<HoldingMonthlyReturns>empty().withErrors(
        allowed);

    ReturnsSnapshot<HoldingMonthlyReturns> result = ReturnsErrorPolicy.throwIfFatal(snapshot);

    assertThat(result).isSameAs(snapshot);
  }

  @Test
  void shouldThrow_whenAnyErrorIsNotAllowed() {
    List<BasePceException> mixed = List.of(
        ErrorCode.HOLDING_PSD_OUT_OF_RANGE.toExceptionForId("allowed"),
        ErrorCode.CPED_BEFORE_PORTFOLIO_PSD.toExceptionForId("fatal"));

    assertThatThrownBy(() -> ReturnsErrorPolicy.throwIfFatal(mixed))
        .isInstanceOf(CalculationsFailedException.class);
  }

  @Test
  void shouldThrowWithAllExceptions_whenFatalErrorPresent() {
    BasePceException allowed = ErrorCode.HOLDING_PSD_OUT_OF_RANGE.toExceptionForId("allowed");
    BasePceException fatal = ErrorCode.CPED_BEFORE_PORTFOLIO_PSD.toExceptionForId("fatal");

    assertThatThrownBy(() -> ReturnsErrorPolicy.throwIfFatal(List.of(allowed, fatal)))
        .isInstanceOf(CalculationsFailedException.class)
        .satisfies(thrown -> {
          CalculationsFailedException ex = (CalculationsFailedException) thrown;
          assertThat(ex.getExceptions()).containsExactly(allowed, fatal);
        });
  }

  @Test
  void shouldNotThrow_whenEmptyExceptionList() {
    assertThatCode(() -> ReturnsErrorPolicy.throwIfFatal(List.of()))
        .doesNotThrowAnyException();
  }

  @Test
  void shouldExposeAllowedErrorCodes_whenAllowedErrorCodesIsCalled() {
    assertThat(ReturnsErrorPolicy.allowedErrorCodes())
        .containsExactlyInAnyOrder(
            ErrorCode.HOLDING_PSD_OUT_OF_RANGE,
            ErrorCode.MISSING_MONTHLY_RETURNS,
            ErrorCode.HOLDING_MISSING_CURRENCY_FROM_MIC,
            ErrorCode.CPED_AFTER_PORTFOLIO_PED);
  }
}
