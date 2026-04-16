package com.fintex.ce.application.validation;

import com.fintex.ce.model.error.ErrorCode;

import lombok.EqualsAndHashCode;

import static com.fintex.ce.model.error.ErrorCode.ERR_RRC_BMPED_002;
import static com.fintex.ce.model.error.ErrorCode.ERR_RRC_BMPED_003;

@EqualsAndHashCode
public class BenchmarkCpedDataValidation extends CpedDataValidation {

  @Override
  public ErrorCode getCpedIsAfterPedExceptionCode() {
    return ERR_RRC_BMPED_003;
  }

  @Override
  public ErrorCode getCpedIsBeforePsdExceptionCode() {
    return ERR_RRC_BMPED_002;
  }
}
