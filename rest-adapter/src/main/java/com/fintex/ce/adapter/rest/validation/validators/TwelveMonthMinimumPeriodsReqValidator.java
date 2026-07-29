package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.SupportedPeriods;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Risk metrics: at least twelve months, and no data-defined period. A statistic over fewer than twelve monthly
 * observations is not meaningful, and "year to date" or "since inception" gives a window whose length varies with the
 * data, which these metrics are not specified against.
 */
@Component
@Order(310)
public class TwelveMonthMinimumPeriodsReqValidator extends AbstractSupportedPeriodsReqValidator<PeriodCommand> {

  public TwelveMonthMinimumPeriodsReqValidator() {
    super(PeriodCommand.class, PeriodCommand::getPeriods, SupportedPeriods.TWELVE_MONTH_MINIMUM);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.TWELVE_MONTH_MINIMUM_METRICS;
  }
}
