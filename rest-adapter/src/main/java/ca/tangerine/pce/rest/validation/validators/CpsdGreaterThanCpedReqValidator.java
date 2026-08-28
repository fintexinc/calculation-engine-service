package ca.tangerine.pce.rest.validation.validators;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.dto.command.contract.CustomPedProvider;
import ca.tangerine.pce.model.dto.command.contract.CustomPsdProvider;
import ca.tangerine.pce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static ca.tangerine.pce.model.domain.enumeration.CalculationMetric.ANNUAL_RETURNS;
import static ca.tangerine.pce.model.domain.enumeration.CalculationMetric.GROWTH_OF_10K;

@Component
@Order(211)
public class CpsdGreaterThanCpedReqValidator extends AbstractDateNotAfterReqValidator {

  public CpsdGreaterThanCpedReqValidator() {
    super(
        cmd -> cmd instanceof CustomPsdProvider p ? p.getCustomPsd() : null,
        cmd -> cmd instanceof CustomPedProvider p ? p.getCustomPed() : null,
        ErrorCode.CPSD_AFTER_CPED);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(ANNUAL_RETURNS, GROWTH_OF_10K);
  }
}
