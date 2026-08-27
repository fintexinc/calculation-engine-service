package ca.tangerine.pce.rest.validation.validators;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static ca.tangerine.pce.model.domain.enumeration.CalculationMetric.ANNUAL_RETURNS;
import static ca.tangerine.pce.model.domain.enumeration.CalculationMetric.GROWTH_OF_10K;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.dto.command.contract.CustomPsdProvider;
import ca.tangerine.pce.model.error.ErrorCode;

@Component
@Order(202)
public class CpsdLastDayOfMonthReqValidator extends AbstractLastDayOfMonthReqValidator<CustomPsdProvider> {

  public CpsdLastDayOfMonthReqValidator() {
    super(CustomPsdProvider.class, CustomPsdProvider::getCustomPsd, ErrorCode.CPSD_NOT_MONTH_END);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(ANNUAL_RETURNS, GROWTH_OF_10K);
  }
}
