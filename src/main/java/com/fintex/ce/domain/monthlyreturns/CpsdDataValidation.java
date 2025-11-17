package com.fintex.ce.domain.monthlyreturns;

import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.exception.notification.pattern.Notification;

import java.time.LocalDate;

import static java.util.Objects.nonNull;

public abstract class CpsdDataValidation {

    public void validate(LocalDate cpsd, LocalDate psd, LocalDate ped, Notification notification) {
        if (nonNull(cpsd) && cpsd.isAfter(ped)) {
            notification.addError(getCpsdIsAfterPedExceptionCode().error());
        }
        if (nonNull(cpsd) && cpsd.isBefore(psd)) {
            notification.addError(getCpsdIsBeforePsdExceptionCode().error());
        }
    }

    protected abstract ExceptionCode getCpsdIsBeforePsdExceptionCode();

    protected abstract ExceptionCode getCpsdIsAfterPedExceptionCode();

}
