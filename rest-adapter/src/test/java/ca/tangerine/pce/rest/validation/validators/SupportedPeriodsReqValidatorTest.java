package ca.tangerine.pce.rest.validation.validators;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.enumeration.SupportedPeriods;
import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.ValidationException;
import ca.tangerine.pce.rest.validation.RequestValidator;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.CIPSD;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.ONE_MTH;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.OVERALL;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.SEVEN_MTH;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.SI;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.SIX_MTH;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.TEN_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.TWENTY_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.YTD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The period contracts, tested through the validators that declare them rather than one class at a time: the rule is
 * the same subset check everywhere, and what differs per metric family is only which set it is checked against.
 *
 * <p>
 * The two coverage tests are the ones that earn their place. A period reaches its calculation unchecked not because the
 * subset check is wrong but because no validator claimed the metric, which is invisible in any single class.
 */
class SupportedPeriodsReqValidatorTest {

  private static final List<RequestValidator> REPORTING_PERIOD_VALIDATORS = List.of(
      new TrailingPeriodsReqValidator(),
      new StandardDeviationPeriodsReqValidator(),
      new TwelveMonthMinimumPeriodsReqValidator());

  @Test
  void shouldClaimEveryReportingPeriodMetricExactlyOnce() {
    List<CalculationMetric> claimed = claimedBy(REPORTING_PERIOD_VALIDATORS);

    assertThat(claimed).containsExactlyInAnyOrderElementsOf(CalculationMetric.PERIOD_METRICS);
    assertThat(claimed).doesNotHaveDuplicates();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("admissible")
  void shouldAccept_whenTheMetricFamilyCanReportThePeriod(
      String scenario,
      RequestValidator validator,
      CalculationCommand command) {
    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("inadmissible")
  void shouldReject_whenThePeriodIsRealButWrongForTheMetricFamily(
      String scenario,
      RequestValidator validator,
      CalculationCommand command) {
    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(thrown -> assertThat(((ValidationException) thrown).getErrorCode())
            .isEqualTo(ErrorCode.TIME_INTERVAL_PERIOD_NOT_SUPPORTED));
  }

  /** A request naming no period takes the configured default set, which {@code PeriodProperties} has already vetted. */
  @Test
  void shouldAcceptAnAbsentPeriodSet_becauseTheDefaultsApplyInstead() {
    PeriodCommand noPeriods = new PeriodCommand();

    assertThatCode(() -> new StandardDeviationPeriodsReqValidator().validate(noPeriods)).doesNotThrowAnyException();
  }

  @Test
  void shouldNameTheOffendingPeriodTheClaimedMetricsAndListTheAdmissibleOnesAsALadder() {
    CalculationCommand command = periodCommand(ONE_YR, CIPSD);

    assertThatThrownBy(() -> new StandardDeviationPeriodsReqValidator().validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(thrown -> {
          ValidationException exception = (ValidationException) thrown;
          assertThat(exception.getMessage()).startsWith(
              "Time interval period 'CIPSD' is not supported for metrics "
                  + CalculationMetric.STANDARD_DEVIATION.getValue()
                  + ". Supported periods: ");
          assertThat(exception.getMetadata()).containsEntry("param-1", "CIPSD");
          assertThat(listedPeriods(exception.getMessage()))
              .containsExactlyElementsOf(fixedLengthNamesShortestFirst())
              .doesNotContain(YTD.name(), SI.name(), CIPSD.name());
        });
  }

  private static Stream<Arguments> admissible() {
    return Stream.of(
        Arguments.of("trailing returns take a request-defined window", new TrailingPeriodsReqValidator(),
            periodCommand(SEVEN_MTH, YTD, SI, CIPSD)),
        Arguments.of("standard deviation takes any fixed length", new StandardDeviationPeriodsReqValidator(),
            periodCommand(ONE_MTH, ONE_YR, TWENTY_YR)),
        Arguments.of("risk metrics take a year or more", new TwelveMonthMinimumPeriodsReqValidator(),
            periodCommand(ONE_YR, TEN_YR)));
  }

  private static Stream<Arguments> inadmissible() {
    return Stream.of(
        Arguments.of("trailing returns and a period that spans no time at all", new TrailingPeriodsReqValidator(),
            periodCommand(ONE_YR, OVERALL)),
        Arguments.of("standard deviation and year to date", new StandardDeviationPeriodsReqValidator(),
            periodCommand(YTD)),
        Arguments.of("standard deviation and since inception", new StandardDeviationPeriodsReqValidator(),
            periodCommand(SI)),
        Arguments.of("standard deviation and a request-defined window", new StandardDeviationPeriodsReqValidator(),
            periodCommand(ONE_YR, CIPSD)),
        Arguments.of("risk metrics and half a year", new TwelveMonthMinimumPeriodsReqValidator(),
            periodCommand(SIX_MTH)),
        Arguments.of("risk metrics and year to date", new TwelveMonthMinimumPeriodsReqValidator(),
            periodCommand(ONE_YR, YTD)));
  }

  private static List<CalculationMetric> claimedBy(final List<RequestValidator> validators) {
    return validators.stream().flatMap(validator -> validator.supportedMetrics().stream()).toList();
  }

  private static CalculationCommand periodCommand(final TimePeriod... periods) {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(new LinkedHashSet<>(List.of(periods)));
    return command;
  }

  private static List<String> listedPeriods(final String message) {
    return List.of(message.substring(message.indexOf("Supported periods: ") + "Supported periods: ".length())
        .split(", "));
  }

  /** The ladder the error message promises, derived from length alone rather than from the production comparator. */
  private static List<String> fixedLengthNamesShortestFirst() {
    return SupportedPeriods.FIXED_LENGTH.stream()
        .sorted(Comparator.comparingInt(TimePeriod::getMonths))
        .map(Enum::name)
        .toList();
  }
}
