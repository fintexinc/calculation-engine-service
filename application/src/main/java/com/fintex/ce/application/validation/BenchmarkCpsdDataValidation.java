package com.fintex.ce.application.validation;

import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_RRC_BMPSD_002;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_RRC_BMPSD_003;

@EqualsAndHashCode
public class BenchmarkCpsdDataValidation extends CpsdDataValidation {

  @Override
  public ExceptionCode getCpsdIsBeforePsdExceptionCode() {
    return ERR_RRC_BMPSD_002;
  }

  @Override
  public ExceptionCode getCpsdIsAfterPedExceptionCode() {
    return ERR_RRC_BMPSD_003;
  }
}
