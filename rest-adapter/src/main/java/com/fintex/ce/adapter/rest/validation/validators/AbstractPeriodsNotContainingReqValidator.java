package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.domain.enumeration.Period;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.util.CollectionUtils;

import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * Base validator that rejects the presence of a given non-numeric {@link Period} symbol inside either
 * {@link PeriodCommand#getPeriods()} or {@link RollingCalculationCommand#getRollingPeriods()}. Subclasses declare which
 * period is disallowed and which {@link ErrorCode} to throw.
 */
public abstract class AbstractPeriodsNotContainingReqValidator implements RequestValidator {

  private final Period disallowedPeriod;
  private final ErrorCode errorCode;

  protected AbstractPeriodsNotContainingReqValidator(Period disallowedPeriod, ErrorCode errorCode) {
    this.disallowedPeriod = disallowedPeriod;
    this.errorCode = errorCode;
  }

  @Override
  public void validate(CalculationCommand command) {
    Set<String> periods;
    if (command instanceof RollingCalculationCommand rc) {
      periods = rc.getRollingPeriods();
    } else if (command instanceof PeriodCommand pc) {
      periods = pc.getPeriods();
    } else {
      return;
    }
    if (CollectionUtils.isEmpty(periods)) {
      return;
    }
    for (String period : periods) {
      if (!isNumeric(period) && period.equals(disallowedPeriod.name())) {
        throw errorCode.reqValidationError();
      }
    }
  }
}
