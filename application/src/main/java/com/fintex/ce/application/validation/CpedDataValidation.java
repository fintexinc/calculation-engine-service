package com.fintex.ce.application.validation;

import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import com.fintex.ce.domain.exception.notification.pattern.Notification;

import java.time.LocalDate;

import static java.util.Objects.nonNull;

public abstract class CpedDataValidation {

  public void validate(final LocalDate cped, final LocalDate psd, final LocalDate ped, Notification notification) {
    validateCpedIsAfterPed(cped, ped, notification);
    validateCpedIsBeforePsd(cped, psd, notification);
  }

  public void validateCpedIsBeforePsd(LocalDate cped, LocalDate psd, Notification notification) {
    if (nonNull(cped) && cped.isBefore(psd)) {
      notification.addError(getCpedIsBeforePsdExceptionCode().error());
    }
  }

  public void validateCpedIsAfterPed(LocalDate cped, LocalDate ped, Notification notification) {
    if (nonNull(cped) && cped.isAfter(ped)) {
      notification.addError(getCpedIsAfterPedExceptionCode().error());
    }
  }

  public abstract ExceptionCode getCpedIsAfterPedExceptionCode();

  public abstract ExceptionCode getCpedIsBeforePsdExceptionCode();

}
