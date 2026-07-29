package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.SupportedPeriods;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.model.domain.enumeration.CalculationMetric.DISTRIBUTION_OF_MONTHLY_RETURNS;
import static com.fintex.ce.model.domain.enumeration.CalculationMetric.MAX_DRAWDOWN;
import static com.fintex.ce.model.domain.enumeration.CalculationMetric.TRAILING_TOTAL_RETURNS;

/**
 * The period metrics that look back from the latest return and are not bound by the twelve-month minimum, and so accept
 * the widest set: any fixed length plus the data- and request-defined windows.
 *
 * <p>
 * Leading returns are the exception among the unbounded metrics and take {@link LeadingPeriodsReqValidator} instead: a
 * window resolved from the end of the series says nothing about one counted forward from its start.
 */
@Component
@Order(310)
public class TrailingPeriodsReqValidator extends AbstractSupportedPeriodsReqValidator<PeriodCommand> {

  public TrailingPeriodsReqValidator() {
    super(PeriodCommand.class, PeriodCommand::getPeriods, SupportedPeriods.TRAILING_RETURNS);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(TRAILING_TOTAL_RETURNS, MAX_DRAWDOWN, DISTRIBUTION_OF_MONTHLY_RETURNS);
  }
}
