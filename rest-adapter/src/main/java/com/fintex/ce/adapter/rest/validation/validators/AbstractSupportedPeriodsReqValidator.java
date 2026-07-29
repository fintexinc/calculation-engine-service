package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.domain.enumeration.SupportedPeriods;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

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
          throw ErrorCode.TIME_INTERVAL_PERIOD_NOT_SUPPORTED.toValidationException(period.name(), admissibleNames());
        });
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
