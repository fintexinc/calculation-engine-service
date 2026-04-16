package com.fintex.ce.application.validation;

import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.Notification;

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

  public abstract ErrorCode getCpsdIsBeforePsdExceptionCode();

  public abstract ErrorCode getCpsdIsAfterPedExceptionCode();

}
