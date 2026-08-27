package ca.tangerine.pce.application.returns;

import java.util.List;
import java.util.Set;

import static ca.tangerine.pce.model.error.ErrorCode.CPED_AFTER_PORTFOLIO_PED;
import static ca.tangerine.pce.model.error.ErrorCode.HOLDING_MISSING_CURRENCY_FROM_MIC;
import static ca.tangerine.pce.model.error.ErrorCode.HOLDING_PSD_OUT_OF_RANGE;
import static ca.tangerine.pce.model.error.ErrorCode.MISSING_MONTHLY_RETURNS;

import ca.tangerine.pce.model.domain.calculation.returns.ReturnsData;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.BasePceException;
import ca.tangerine.pce.model.error.exceptions.CalculationsFailedException;

/**
 * Stateless utility encoding which accumulated errors are recoverable warnings and which are fatal — i.e. which cause
 * the calculation to abort with a {@link CalculationsFailedException}.
 *
 * <p>
 * The allowed-error set mirrors the list previously hard-coded in {@code ReturnsAggregate.ifAnyErrorsThrowException}.
 * Errors carrying any of these codes are passed downstream as warnings; any other error code triggers an immediate
 * throw the next time {@link #throwIfFatal(ReturnsSnapshot)} is invoked.
 * </p>
 */
public final class ReturnsErrorPolicy {

  private static final Set<ErrorCode> ALLOWED_ERROR_CODES = Set.of(
      HOLDING_PSD_OUT_OF_RANGE,
      MISSING_MONTHLY_RETURNS,
      HOLDING_MISSING_CURRENCY_FROM_MIC,
      CPED_AFTER_PORTFOLIO_PED);

  private ReturnsErrorPolicy() {
  }

  public static <T extends ReturnsData> ReturnsSnapshot<T> throwIfFatal(ReturnsSnapshot<T> snapshot) {
    throwIfFatal(snapshot.errors());
    return snapshot;
  }

  public static void throwIfFatal(List<BasePceException> exceptions) {
    if (exceptions.isEmpty()) {
      return;
    }
    if (exceptions.stream().anyMatch(
        exception -> !ALLOWED_ERROR_CODES.contains(exception.getErrorCode()))) {
      throw new CalculationsFailedException(exceptions);
    }
  }

  public static Set<ErrorCode> allowedErrorCodes() {
    return ALLOWED_ERROR_CODES;
  }
}
