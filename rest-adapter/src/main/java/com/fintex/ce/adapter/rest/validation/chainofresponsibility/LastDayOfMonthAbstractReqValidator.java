package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import java.time.LocalDate;
import lombok.EqualsAndHashCode;

import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;

@EqualsAndHashCode(callSuper = true)
public abstract class LastDayOfMonthAbstractReqValidator extends ReqValidation {

  private final LocalDate date;

  public LastDayOfMonthAbstractReqValidator(final LocalDate date) {
    this.date = date;
  }

  @Override
  public void check() {
    if (date != null && !date.equals(toLastDayOfMonth(date))) {
      throwException();
    }
  }

  abstract protected void throwException();
}
