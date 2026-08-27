package ca.tangerine.pce.rest.validation.validators;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static ca.tangerine.pce.model.domain.enumeration.CalculationMetric.*;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.dto.command.contract.CustomPedProvider;
import ca.tangerine.pce.model.error.ErrorCode;

@Component
@Order(201)
public class CpedLastDayOfMonthReqValidator extends AbstractLastDayOfMonthReqValidator<CustomPedProvider> {

  public CpedLastDayOfMonthReqValidator() {
    super(CustomPedProvider.class, CustomPedProvider::getCustomPed, ErrorCode.CPED_NOT_MONTH_END);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(
        TRAILING_TOTAL_RETURNS, STANDARD_DEVIATION, SHARPE_RATIO, MAX_DRAWDOWN, ANNUAL_RETURNS, GROWTH_OF_10K);
  }
}
