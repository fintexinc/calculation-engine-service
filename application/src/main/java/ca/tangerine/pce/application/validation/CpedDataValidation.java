package ca.tangerine.pce.application.validation;

import ca.tangerine.pce.application.returns.ProcessingContext;
import ca.tangerine.pce.application.returns.ReturnsSnapshot;
import ca.tangerine.pce.model.domain.calculation.returns.ReturnsData;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.BasePceException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifies that a request's custom performance end date sits within the inferred performance window. Subclasses bind
 * the abstract error-code accessors to portfolio- or benchmark-specific codes.
 */
public abstract class CpedDataValidation extends AbstractReturnsValidation {

  @Override
  protected <T extends ReturnsData> List<BasePceException> collectErrors(ReturnsSnapshot<T> snapshot,
      ProcessingContext context) {
    LocalDate cped = context.cped();
    if (cped == null) {
      return List.of();
    }
    var errors = new ArrayList<BasePceException>(2);
    LocalDate performanceEndDate = snapshot.performanceEndDate();
    if (performanceEndDate != null && cped.isAfter(performanceEndDate)) {
      errors.add(getCpedIsAfterPedExceptionCode().toException());
    }
    LocalDate performanceStartDate = snapshot.performanceStartDate();
    if (performanceStartDate != null && cped.isBefore(performanceStartDate)) {
      errors.add(getCpedIsBeforePsdExceptionCode().toException());
    }
    return errors;
  }

  protected abstract ErrorCode getCpedIsAfterPedExceptionCode();

  protected abstract ErrorCode getCpedIsBeforePsdExceptionCode();
}
