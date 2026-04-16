package com.fintex.ce.application.validation;

import com.fintex.ce.model.error.ErrorCode;

import lombok.EqualsAndHashCode;

import static com.fintex.ce.model.error.ErrorCode.ERR_RRC_CPSD_002;
import static com.fintex.ce.model.error.ErrorCode.ERR_RRC_CPSD_003;

@EqualsAndHashCode
public class PortfolioCpsdDataValidation extends CpsdDataValidation {

  @Override
  public ErrorCode getCpsdIsBeforePsdExceptionCode() {
    return ERR_RRC_CPSD_002;
  }

  @Override
  public ErrorCode getCpsdIsAfterPedExceptionCode() {
    return ERR_RRC_CPSD_003;
  }

}
