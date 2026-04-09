package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.enumeration.Period;

import org.springframework.util.CollectionUtils;

import java.util.Set;
import lombok.EqualsAndHashCode;

import static org.apache.commons.lang3.StringUtils.isNumeric;

@EqualsAndHashCode(callSuper = true)
public abstract class PeriodsNotContainingAbstractReqValidation extends ReqValidation {

  private final Set<String> periods;

  public PeriodsNotContainingAbstractReqValidation(final Set<String> periods) {
    this.periods = periods;
  }

  @Override
  public void check() {
    if (CollectionUtils.isEmpty(periods)) {
      return;
    }
    for (final String period : periods) {
      if (!isNumeric(period) && period.equals(getNotAllowedPeriod().name())) {
        throwException();
      }
    }
  }

  public abstract Period getNotAllowedPeriod();

  public abstract void throwException();
}
