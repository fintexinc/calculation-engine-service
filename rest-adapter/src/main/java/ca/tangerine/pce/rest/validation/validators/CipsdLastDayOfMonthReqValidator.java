package ca.tangerine.pce.rest.validation.validators;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(200)
public class CipsdLastDayOfMonthReqValidator extends AbstractLastDayOfMonthReqValidator<PeriodCommand> {

  public CipsdLastDayOfMonthReqValidator() {
    super(PeriodCommand.class, PeriodCommand::getCustomIntervalPsd, ErrorCode.CIPSD_NOT_MONTH_END);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.CIPSD_SUPPORTED_METRICS;
  }
}
