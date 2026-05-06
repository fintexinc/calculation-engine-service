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
