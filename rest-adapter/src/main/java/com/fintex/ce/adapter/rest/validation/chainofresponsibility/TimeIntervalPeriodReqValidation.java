package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
public class TimeIntervalPeriodReqValidation extends ReqValidation {

  private final Integer period;

  public TimeIntervalPeriodReqValidation(final Integer period) {
    this.period = period;
  }

  @Override
  protected void check() {
    if (Objects.nonNull(period) && period <= 0) {
      throw ExceptionCode.ERR_RRC_TIP_003.reqValidationError();
    }
  }

}
