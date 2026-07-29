package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.SupportedPeriods;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.model.domain.enumeration.CalculationMetric.ROLLING_SHARPE_RATIO;
import static com.fintex.ce.model.domain.enumeration.CalculationMetric.ROLLING_STANDARD_DEVIATION;

/**
 * Rolling windows over a statistic, which need the same twelve-month minimum as the statistic they roll. Applies to the
 * rolling window itself ({@code rollingTimeIntervalPeriod}), not to the reporting periods the parent command carries.
 *
 * <p>
 * The rolling metrics that are not statistics take {@link RollingFixedLengthPeriodsReqValidator} instead. Between them
 * the two cover {@link CalculationMetric#ROLLING_METRICS} exactly, which {@code SupportedPeriodsReqValidatorTest} pins:
 * a metric named by neither would reach its calculation with a window nothing had checked.
 */
@Component
@Order(331)
public class RollingTwelveMonthMinimumPeriodsReqValidator
    extends
      AbstractSupportedPeriodsReqValidator<RollingCalculationCommand> {

  public RollingTwelveMonthMinimumPeriodsReqValidator() {
    super(RollingCalculationCommand.class, RollingCalculationCommand::getRollingPeriods,
        SupportedPeriods.TWELVE_MONTH_MINIMUM);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(ROLLING_STANDARD_DEVIATION, ROLLING_SHARPE_RATIO);
  }
}
