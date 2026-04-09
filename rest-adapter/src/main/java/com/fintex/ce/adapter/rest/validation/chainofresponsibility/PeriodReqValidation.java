package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import com.fintex.ce.domain.model.enumeration.Period;

import org.springframework.util.CollectionUtils;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;

import static org.apache.commons.lang3.StringUtils.isNumeric;

@EqualsAndHashCode(callSuper = true)
public class PeriodReqValidation extends ReqValidation {

  private final Set<String> periods;

  public PeriodReqValidation(final Set<String> periods) {
    this.periods = periods;
  }

  @Override
  public void check() {
    if (CollectionUtils.isEmpty(periods)) {
      return;
    }

    final Set<String> allowedSymbols = Stream.of(Period.values()).map(Enum::name).collect(Collectors.toSet());
    for (String period : periods) {
      if (NumberUtils.isNumber(period) && Integer.parseInt(period) <= 0) {
        throw ExceptionCode.ERR_RRC_TIP_003.reqValidationError();
      }
      if (!isNumeric(period) && !allowedSymbols.contains(period)) {
        throw ExceptionCode.ERR_RRC_TIP_004.reqValidationError(period);
      }
    }
  }
}
