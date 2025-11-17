package com.fintex.ce.domain.monthlyreturns;

import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.exception.notification.pattern.Notification;

import java.time.LocalDate;

import static java.util.Objects.nonNull;

public abstract class CpedDataValidation {

    public void validate(final LocalDate cped, final LocalDate psd, final LocalDate ped, Notification notification) {
        validateCpedIsAfterPed(cped, ped, notification);
        validateCpedIsBeforePsd(cped, psd, notification);
    }

    protected void validateCpedIsBeforePsd(LocalDate cped, LocalDate psd, Notification notification) {
        if (nonNull(cped) && cped.isBefore(psd)) {
            notification.addError(getCpedIsBeforePsdExceptionCode().error());
        }
    }

    protected void validateCpedIsAfterPed(LocalDate cped, LocalDate ped, Notification notification) {
        if (nonNull(cped) && cped.isAfter(ped)) {
            notification.addError(getCpedIsAfterPedExceptionCode().error());
        }
    }

    abstract protected ExceptionCode getCpedIsAfterPedExceptionCode();

    abstract protected ExceptionCode getCpedIsBeforePsdExceptionCode();

}
