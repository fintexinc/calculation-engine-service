package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.model.domain.enumeration.CalculationMetric.*;

@Component
@Order(331)
public class RollingPeriodsLessThan12ReqValidator
    extends
      AbstractPeriodsLessThan12ReqValidator<RollingCalculationCommand> {

  public RollingPeriodsLessThan12ReqValidator() {
    super(RollingCalculationCommand.class, RollingCalculationCommand::getRollingPeriods,
        ErrorCode.ROLLING_INTERVAL_LESS_THAN_12);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(ROLLING_STANDARD_DEVIATION, ROLLING_SHARPE_RATIO);
  }
}
