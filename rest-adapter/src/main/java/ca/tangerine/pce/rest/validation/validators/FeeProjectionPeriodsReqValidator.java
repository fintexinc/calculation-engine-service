package ca.tangerine.pce.rest.validation.validators;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.enumeration.SupportedPeriods;
import ca.tangerine.pce.model.dto.command.AverageMerCommand;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fee projections: the agreed reporting ladder, and nothing without a length.
 *
 * <p>
 * A projection forward has no answer for "year to date" or "since inception", so those are excluded rather than
 * accepted and answered with null. The fixed lengths are narrowed to the ladder the report actually shows — the
 * arithmetic would handle {@code SEVEN_YR} perfectly well, but every extra column is one the report has to explain.
 *
 * <p>
 * This replaces a hand-written check that a bare {@code Integer} year count was at least 1. That state is now
 * unrepresentable, so what is left is the same subset question every other period contract asks.
 */
@Component
@Order(320)
public class FeeProjectionPeriodsReqValidator extends AbstractSupportedPeriodsReqValidator<AverageMerCommand> {

  public FeeProjectionPeriodsReqValidator() {
    super(AverageMerCommand.class, AverageMerCommand::getProjectionPeriods, SupportedPeriods.FEE_PROJECTION);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.FEE_PROJECTION_METRICS;
  }
}
