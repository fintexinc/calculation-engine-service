package com.fintex.ce.application.validation;

import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.PceExceptionCollector;

import java.time.LocalDate;

import static java.util.Objects.nonNull;

public abstract class CpedDataValidation {

  public void validate(final LocalDate cped, final LocalDate psd, final LocalDate ped,
      PceExceptionCollector notification) {
    validateCpedIsAfterPed(cped, ped, notification);
    validateCpedIsBeforePsd(cped, psd, notification);
  }

  public void validateCpedIsBeforePsd(LocalDate cped, LocalDate psd, PceExceptionCollector notification) {
    if (nonNull(cped) && cped.isBefore(psd)) {
      notification.add(getCpedIsBeforePsdExceptionCode().toException());
    }
  }

  public void validateCpedIsAfterPed(LocalDate cped, LocalDate ped, PceExceptionCollector notification) {
    if (nonNull(cped) && cped.isAfter(ped)) {
      notification.add(getCpedIsAfterPedExceptionCode().toException());
    }
  }

  public abstract ErrorCode getCpedIsAfterPedExceptionCode();

  public abstract ErrorCode getCpedIsBeforePsdExceptionCode();

}
