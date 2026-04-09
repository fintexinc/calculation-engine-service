package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.enumeration.ExceptionCode;

import org.springframework.util.CollectionUtils;

import java.util.Set;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class RollingPeriodsLessThan12ReqValidation extends ReqValidation {

  private final Set<String> periods;

  public RollingPeriodsLessThan12ReqValidation(final Set<String> periods) {
    this.periods = periods;
  }

  @Override
  public void check() {
    if (CollectionUtils.isEmpty(periods)) {
      return;
    }
    for (final var period : periods) {
      if (Long.parseLong(period) < 12) {
        throw ExceptionCode.ERR_RRC_RTIP_001.reqValidationError();
      }
    }
  }
}
