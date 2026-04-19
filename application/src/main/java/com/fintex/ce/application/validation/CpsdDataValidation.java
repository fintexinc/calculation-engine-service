package com.fintex.ce.application.validation;

import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.PceExceptionCollector;

import java.time.LocalDate;

import static java.util.Objects.nonNull;

public abstract class CpsdDataValidation {

  public void validate(LocalDate cpsd, LocalDate psd, LocalDate ped, PceExceptionCollector notification) {
    if (nonNull(cpsd) && cpsd.isAfter(ped)) {
      notification.add(getCpsdIsAfterPedExceptionCode().toException());
    }
    if (nonNull(cpsd) && cpsd.isBefore(psd)) {
      notification.add(getCpsdIsBeforePsdExceptionCode().toException());
    }
  }

  public abstract ErrorCode getCpsdIsBeforePsdExceptionCode();

  public abstract ErrorCode getCpsdIsAfterPedExceptionCode();

}
