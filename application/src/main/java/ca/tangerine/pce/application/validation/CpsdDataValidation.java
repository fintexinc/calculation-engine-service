package ca.tangerine.pce.application.validation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ca.tangerine.pce.application.returns.ProcessingContext;
import ca.tangerine.pce.application.returns.ReturnsSnapshot;
import ca.tangerine.pce.model.domain.calculation.returns.ReturnsData;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.BasePceException;

/**
 * Verifies that a request's custom performance start date sits within the inferred performance window. Subclasses bind
 * the abstract error-code accessors to portfolio- or benchmark-specific codes.
 */
public abstract class CpsdDataValidation extends AbstractReturnsValidation {

  @Override
  protected <T extends ReturnsData> List<BasePceException> collectErrors(ReturnsSnapshot<T> snapshot,
      ProcessingContext context) {
    LocalDate cpsd = context.cpsd();
    if (cpsd == null) {
      return List.of();
    }
    var errors = new ArrayList<BasePceException>(2);
    LocalDate performanceEndDate = snapshot.performanceEndDate();
    if (performanceEndDate != null && cpsd.isAfter(performanceEndDate)) {
      errors.add(getCpsdIsAfterPedExceptionCode().toException());
    }
    LocalDate performanceStartDate = snapshot.performanceStartDate();
    if (performanceStartDate != null && cpsd.isBefore(performanceStartDate)) {
      errors.add(getCpsdIsBeforePsdExceptionCode().toException());
    }
    return errors;
  }

  protected abstract ErrorCode getCpsdIsBeforePsdExceptionCode();

  protected abstract ErrorCode getCpsdIsAfterPedExceptionCode();
}
