package ca.tangerine.pce.rest.validation.validators;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.error.ErrorCode;

@Component
@Order(210)
public class CipsdGreaterThanCpedReqValidator extends AbstractDateNotAfterReqValidator {

  public CipsdGreaterThanCpedReqValidator() {
    super(
        cmd -> cmd instanceof PeriodCommand pc ? pc.getCustomIntervalPsd() : null,
        cmd -> cmd instanceof PeriodCommand pc ? pc.getCustomPed() : null,
        ErrorCode.CIPSD_AFTER_CPED);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.CIPSD_SUPPORTED_METRICS;
  }
}
