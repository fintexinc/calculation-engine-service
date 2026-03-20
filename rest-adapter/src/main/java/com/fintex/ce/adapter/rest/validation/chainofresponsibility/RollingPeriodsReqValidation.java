package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;
import org.springframework.util.CollectionUtils;

import java.util.Set;

import static com.fintex.ce.util.CalculationUtils.isNegativeNumeric;
import static org.apache.commons.lang3.StringUtils.isNumeric;

@EqualsAndHashCode(callSuper = true)
public class RollingPeriodsReqValidation extends ReqValidation {

  private final Set<String> periods;

  public RollingPeriodsReqValidation(final Set<String> periods) {
    this.periods = periods;
  }

  @Override
  public void check() {
    if (CollectionUtils.isEmpty(periods)) {
      return;
    }
    for (final var period : periods) {
      if (!isNumeric(period) && !isNegativeNumeric(period)) {
        throw ExceptionCode.ERR_RRC_TIP_004.reqValidationError(period);
      }
      if (Long.parseLong(period) <= 0) {
        throw ExceptionCode.ERR_RRC_RTIP_003.reqValidationError();
      }
    }
  }
}
