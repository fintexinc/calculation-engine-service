package com.fintex.ce.application.validation;

import com.fintex.ce.model.error.ErrorCode;

import lombok.EqualsAndHashCode;

import static com.fintex.ce.model.error.ErrorCode.CPED_AFTER_PORTFOLIO_PED;
import static com.fintex.ce.model.error.ErrorCode.CPED_BEFORE_PORTFOLIO_PSD;

@EqualsAndHashCode
public class PortfolioCpedDataValidation extends CpedDataValidation {

  @Override
  public ErrorCode getCpedIsAfterPedExceptionCode() {
    return CPED_AFTER_PORTFOLIO_PED;
  }

  @Override
  public ErrorCode getCpedIsBeforePsdExceptionCode() {
    return CPED_BEFORE_PORTFOLIO_PSD;
  }

}
