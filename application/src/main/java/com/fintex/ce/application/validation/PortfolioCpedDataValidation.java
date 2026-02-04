package com.fintex.ce.application.validation;

import com.fintex.ce.domain.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import static com.fintex.ce.domain.enumeration.ExceptionCode.ERR_RRC_CPED_002;
import static com.fintex.ce.domain.enumeration.ExceptionCode.ERR_RRC_CPED_003;

@EqualsAndHashCode
public class PortfolioCpedDataValidation extends CpedDataValidation {

  @Override
  public ExceptionCode getCpedIsAfterPedExceptionCode() {
    return ERR_RRC_CPED_003;
  }

  @Override
  public ExceptionCode getCpedIsBeforePsdExceptionCode() {
    return ERR_RRC_CPED_002;
  }

}
