package com.fintex.ce.application.validation;

import com.fintex.ce.application.returns.ProcessingContext;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.model.domain.calculation.returns.ReturnsData;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.BasePceException;

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
