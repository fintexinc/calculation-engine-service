package com.fintex.ce.application.validation;

import com.fintex.ce.model.error.PceExceptionCollector;

import java.time.LocalDate;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class GrowthOf10KCpedDataValidation extends PortfolioCpedDataValidation {

  @Override
  public void validate(LocalDate cped, LocalDate psd, LocalDate ped, PceExceptionCollector notification) {
    validateCpedIsBeforePsd(cped, psd, notification);
  }
}
