package ca.tangerine.pce.rest.validation.validators;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.enumeration.SupportedPeriods;
import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.rest.validation.RequestValidator;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Rejects a requested period the metric family cannot answer, naming it and listing what it would have accepted.
 *
 * <p>
 * This one check replaces six classes that each re-derived a predicate over an untyped period string:
 * {@code PeriodReqValidator} (is it numeric or a known symbol), {@code PeriodLessThan12ReqValidator} and
 * {@code RollingPeriodsLessThan12ReqValidator} (is it at least twelve months), {@code RollingPeriodsReqValidator} (is
 * it positive), and three {@code PeriodsNotContaining*} validators (is it this particular symbol). Once a period is a
 * {@link TimePeriod} and each contract declares its admissible {@link SupportedPeriods} set, all six questions are the
 * same question, and the answer can say what the alternatives are instead of only that the input was wrong.
 *
 * <p>
 * Unparseable values never reach here — they fail deserialization, where {@code TimePeriodJacksonModule} reports them
 * with the same error code. This validator handles the values that are real periods but wrong for the metric asked for.
 */
public abstract class AbstractSupportedPeriodsReqValidator<T extends CalculationCommand> implements RequestValidator {

  private final Class<T> carrierType;
  private final Function<T, Set<TimePeriod>> periodsAccessor;
  private final Set<TimePeriod> admissible;

  protected AbstractSupportedPeriodsReqValidator(
      final Class<T> carrierType,
      final Function<T, Set<TimePeriod>> periodsAccessor,
      final Set<TimePeriod> admissible) {
    this.carrierType = carrierType;
    this.periodsAccessor = periodsAccessor;
    this.admissible = admissible;
  }

  @Override
  public void validate(final CalculationCommand command) {
    if (!carrierType.isInstance(command)) {
      return;
    }
    Set<TimePeriod> periods = periodsAccessor.apply(carrierType.cast(command));
    if (CollectionUtils.isEmpty(periods)) {
      return;
    }
    periods.stream()
        .filter(period -> !admissible.contains(period))
        .findFirst()
        .ifPresent(period -> {
          throw ErrorCode.TIME_INTERVAL_PERIOD_NOT_SUPPORTED
              .toValidationException(period.name(), claimedMetricNames(), admissibleNames());
        });
  }

  /** The metrics this validator speaks for, rendered the same way every other call site renders them. */
  private String claimedMetricNames() {
    return supportedMetrics().stream()
        .map(CalculationMetric::getValue)
        .collect(Collectors.joining(", "));
  }

  /** Fixed lengths in ascending order of length, then the data-defined ones, so the list reads as a ladder. */
  private String admissibleNames() {
    return admissible.stream()
        .sorted(Comparator.comparing(TimePeriod::isFixedLength).reversed()
            .thenComparingInt(period -> period.isFixedLength() ? period.getMonths() : 0))
        .map(Enum::name)
        .collect(Collectors.joining(", "));
  }
}
