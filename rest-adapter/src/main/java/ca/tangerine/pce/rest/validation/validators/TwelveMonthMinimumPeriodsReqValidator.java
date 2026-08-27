package ca.tangerine.pce.rest.validation.validators;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.enumeration.SupportedPeriods;
import ca.tangerine.pce.model.dto.command.PeriodCommand;

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
