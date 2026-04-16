package com.fintex.ce.application.validation;

import com.fintex.ce.model.error.ErrorCode;

import lombok.EqualsAndHashCode;

import static com.fintex.ce.model.error.ErrorCode.ERR_RRC_CPED_002;
import static com.fintex.ce.model.error.ErrorCode.ERR_RRC_CPED_003;

@EqualsAndHashCode
public class PortfolioCpedDataValidation extends CpedDataValidation {

  @Override
  public ErrorCode getCpedIsAfterPedExceptionCode() {
    return ERR_RRC_CPED_003;
  }

  @Override
  public ErrorCode getCpedIsBeforePsdExceptionCode() {
    return ERR_RRC_CPED_002;
  }

}
