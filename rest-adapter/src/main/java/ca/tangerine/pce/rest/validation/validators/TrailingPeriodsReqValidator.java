package ca.tangerine.pce.rest.validation.validators;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static ca.tangerine.pce.model.domain.enumeration.CalculationMetric.MAX_DRAWDOWN;
import static ca.tangerine.pce.model.domain.enumeration.CalculationMetric.TRAILING_TOTAL_RETURNS;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.enumeration.SupportedPeriods;
import ca.tangerine.pce.model.dto.command.PeriodCommand;

/**
 * The period metrics that look back from the latest return and are not bound by the twelve-month minimum, and so accept
 * the widest set: any fixed length plus the data- and request-defined windows.
 */
@Component
@Order(310)
public class TrailingPeriodsReqValidator extends AbstractSupportedPeriodsReqValidator<PeriodCommand> {

  public TrailingPeriodsReqValidator() {
    super(PeriodCommand.class, PeriodCommand::getPeriods, SupportedPeriods.TRAILING_RETURNS);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(TRAILING_TOTAL_RETURNS, MAX_DRAWDOWN);
  }
}
