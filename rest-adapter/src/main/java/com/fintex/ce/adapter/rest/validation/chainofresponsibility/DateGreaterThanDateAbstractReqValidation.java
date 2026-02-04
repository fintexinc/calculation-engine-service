package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
public abstract class DateGreaterThanDateAbstractReqValidation extends ReqValidation {

  private final LocalDate firstDate;
  private final LocalDate secondDate;

  public DateGreaterThanDateAbstractReqValidation(final LocalDate firstDate, final LocalDate secondDate) {
    this.firstDate = firstDate;
    this.secondDate = secondDate;
  }

  @Override
  public void check() {
    if (firstDate != null && secondDate != null && firstDate.isAfter(secondDate)) {
      throwException();
    }
  }

  protected abstract void throwException();

}
