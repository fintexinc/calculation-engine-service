package com.fintex.ce.util.validation.request.chainofresponsibility;

import lombok.EqualsAndHashCode;

import java.time.LocalDate;

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
