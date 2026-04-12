package com.fintex.ce.application.validation;

import com.fintex.ce.domain.exception.code.ErrorCode;

import lombok.EqualsAndHashCode;

import static com.fintex.ce.domain.exception.code.ErrorCode.ERR_RRC_BMPED_002;
import static com.fintex.ce.domain.exception.code.ErrorCode.ERR_RRC_BMPED_003;

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
