package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.SupportedPeriods;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.model.domain.enumeration.CalculationMetric.ROLLING_CORRELATION;
import static com.fintex.ce.model.domain.enumeration.CalculationMetric.ROLLING_TOTAL_RETURNS;

/**
 * The rolling metrics not held to the twelve-month minimum: any fixed length is a window they can report, and only the
 * length-less members are refused. A six-month rolling return is a return over six months, not a statistic asked to
 * mean something over six observations.
 *
 * <p>
 * This is the contract these two metrics had before periods became a typed vocabulary — a window then had to be a
 * positive month count, and nothing narrowed it to twelve. {@link CalculationMetric#ROLLING_CORRELATION} is arguably a
 * statistic and belongs with the others, but moving it changes the contract rather than the vocabulary, so it stays
 * here until that is asked for.
 */
@Component
@Order(331)
public class RollingFixedLengthPeriodsReqValidator
    extends
      AbstractSupportedPeriodsReqValidator<RollingCalculationCommand> {

  public RollingFixedLengthPeriodsReqValidator() {
    super(RollingCalculationCommand.class, RollingCalculationCommand::getRollingPeriods,
        SupportedPeriods.FIXED_LENGTH);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(ROLLING_TOTAL_RETURNS, ROLLING_CORRELATION);
  }
}
