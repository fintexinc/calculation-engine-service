package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.SupportedPeriods;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.model.domain.enumeration.CalculationMetric.LEADING_TOTAL_RETURNS;

/**
 * Leading returns, the one period metric that counts forward from the first observation instead of back from the
 * latest. Fixed lengths only, which is why it cannot share the trailing set.
 *
 * <p>
 * A length the data resolves ({@code YTD}, {@code SI}) or the request resolves ({@code CIPSD}) is measured from a date
 * at the end of the series, while {@code LeadingTotalReturnsCalculation} anchors on the first return and walks forward.
 * Admitting one would not fail — it would answer a window the caller never named, which is worse.
 */
@Component
@Order(310)
public class LeadingPeriodsReqValidator extends AbstractSupportedPeriodsReqValidator<PeriodCommand> {

  public LeadingPeriodsReqValidator() {
    super(PeriodCommand.class, PeriodCommand::getPeriods, SupportedPeriods.FIXED_LENGTH);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(LEADING_TOTAL_RETURNS);
  }
}
