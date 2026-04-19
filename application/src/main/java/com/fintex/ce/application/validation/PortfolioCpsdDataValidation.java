package com.fintex.ce.application.validation;

import com.fintex.ce.model.error.ErrorCode;

import lombok.EqualsAndHashCode;

import static com.fintex.ce.model.error.ErrorCode.CPSD_AFTER_PORTFOLIO_PED;
import static com.fintex.ce.model.error.ErrorCode.CPSD_BEFORE_PORTFOLIO_PSD;

@EqualsAndHashCode
public class PortfolioCpsdDataValidation extends CpsdDataValidation {

  @Override
  public ErrorCode getCpsdIsBeforePsdExceptionCode() {
    return CPSD_BEFORE_PORTFOLIO_PSD;
  }

  @Override
  public ErrorCode getCpsdIsAfterPedExceptionCode() {
    return CPSD_AFTER_PORTFOLIO_PED;
  }

}
