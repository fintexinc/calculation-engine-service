package ca.tangerine.pce.rest.validation.validators;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.enumeration.SupportedPeriods;
import ca.tangerine.pce.model.dto.command.PeriodCommand;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(310)
public class StandardDeviationPeriodsReqValidator extends AbstractSupportedPeriodsReqValidator<PeriodCommand> {

  public StandardDeviationPeriodsReqValidator() {
    super(PeriodCommand.class, PeriodCommand::getPeriods, SupportedPeriods.FIXED_LENGTH);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(CalculationMetric.STANDARD_DEVIATION);
  }
}
