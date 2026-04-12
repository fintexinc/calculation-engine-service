package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.RollingCalculationCommand;
import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.domain.model.enumeration.CalculationMetric.*;

@Component
@Order(331)
public class RollingPeriodsLessThan12ReqValidator
    extends
      AbstractPeriodsLessThan12ReqValidator<RollingCalculationCommand> {

  public RollingPeriodsLessThan12ReqValidator() {
    super(RollingCalculationCommand.class, RollingCalculationCommand::getRollingPeriods,
        ErrorCode.ERR_RRC_RTIP_001);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(ROLLING_STANDARD_DEVIATION, ROLLING_SHARPE_RATIO);
  }
}
