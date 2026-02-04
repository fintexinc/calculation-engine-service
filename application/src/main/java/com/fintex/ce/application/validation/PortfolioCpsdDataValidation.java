package com.fintex.ce.application.validation;

import com.fintex.ce.domain.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import static com.fintex.ce.domain.enumeration.ExceptionCode.ERR_RRC_CPSD_002;
import static com.fintex.ce.domain.enumeration.ExceptionCode.ERR_RRC_CPSD_003;

@EqualsAndHashCode
public class PortfolioCpsdDataValidation extends CpsdDataValidation {

  @Override
  public ExceptionCode getCpsdIsBeforePsdExceptionCode() {
    return ERR_RRC_CPSD_002;
  }

  @Override
  public ExceptionCode getCpsdIsAfterPedExceptionCode() {
    return ERR_RRC_CPSD_003;
  }

}
