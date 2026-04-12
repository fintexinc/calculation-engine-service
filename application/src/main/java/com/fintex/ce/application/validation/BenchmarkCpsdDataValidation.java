package com.fintex.ce.application.validation;

import com.fintex.ce.domain.exception.code.ErrorCode;

import lombok.EqualsAndHashCode;

import static com.fintex.ce.domain.exception.code.ErrorCode.ERR_RRC_BMPSD_002;
import static com.fintex.ce.domain.exception.code.ErrorCode.ERR_RRC_BMPSD_003;

@EqualsAndHashCode
public class BenchmarkCpsdDataValidation extends CpsdDataValidation {

  @Override
  public ErrorCode getCpsdIsBeforePsdExceptionCode() {
    return ERR_RRC_BMPSD_002;
  }

  @Override
  public ErrorCode getCpsdIsAfterPedExceptionCode() {
    return ERR_RRC_BMPSD_003;
  }
}
