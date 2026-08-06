package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.SupportedPeriods;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.ValidationException;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

import static com.fintex.wm.commons.domain.enumeration.TimePeriod.CIPSD;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_MTH;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.OVERALL;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.SEVEN_MTH;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.SI;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.SIX_MTH;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.TEN_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.TWENTY_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.YTD;
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
      new LeadingPeriodsReqValidator(),
      new StandardDeviationPeriodsReqValidator(),
      new TwelveMonthMinimumPeriodsReqValidator());

  private static final List<RequestValidator> ROLLING_WINDOW_VALIDATORS = List.of(
      new RollingTwelveMonthMinimumPeriodsReqValidator(),
      new RollingFixedLengthPeriodsReqValidator());

  @Test
  void shouldClaimEveryReportingPeriodMetricExactlyOnce() {
    List<CalculationMetric> claimed = claimedBy(REPORTING_PERIOD_VALIDATORS);

    assertThat(claimed).containsExactlyInAnyOrderElementsOf(CalculationMetric.PERIOD_METRICS);
    assertThat(claimed).doesNotHaveDuplicates();
  }

  @Test
  void shouldClaimEveryRollingWindowMetricExactlyOnce() {
    List<CalculationMetric> claimed = claimedBy(ROLLING_WINDOW_VALIDATORS);

    assertThat(claimed).containsExactlyInAnyOrderElementsOf(CalculationMetric.ROLLING_METRICS);
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
    RollingCalculationCommand noWindow = new RollingCalculationCommand();

    assertThatCode(() -> new LeadingPeriodsReqValidator().validate(noPeriods)).doesNotThrowAnyException();
    assertThatCode(() -> new RollingFixedLengthPeriodsReqValidator().validate(noWindow)).doesNotThrowAnyException();
  }

  @Test
  void shouldNameTheOffendingPeriodTheClaimedMetricsAndListTheAdmissibleOnesAsALadder() {
    CalculationCommand command = periodCommand(ONE_YR, CIPSD);

    assertThatThrownBy(() -> new LeadingPeriodsReqValidator().validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(thrown -> {
          ValidationException exception = (ValidationException) thrown;
          assertThat(exception.getMessage()).startsWith(
              "Time interval period 'CIPSD' is not supported for metrics "
                  + CalculationMetric.LEADING_TOTAL_RETURNS.getValue()
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
        Arguments.of("leading returns take any fixed length", new LeadingPeriodsReqValidator(),
            periodCommand(ONE_MTH, ONE_YR, TWENTY_YR)),
        Arguments.of("risk metrics take a year or more", new TwelveMonthMinimumPeriodsReqValidator(),
            periodCommand(ONE_YR, TEN_YR)),
        Arguments.of("rolling returns take a window shorter than a year", new RollingFixedLengthPeriodsReqValidator(),
            rollingCommand(SIX_MTH, ONE_YR)),
        Arguments.of("rolling statistics take a window of a year or more",
            new RollingTwelveMonthMinimumPeriodsReqValidator(), rollingCommand(ONE_YR, TWENTY_YR)));
  }

  private static Stream<Arguments> inadmissible() {
    return Stream.of(
        Arguments.of("trailing returns and a period that spans no time at all", new TrailingPeriodsReqValidator(),
            periodCommand(ONE_YR, OVERALL)),
        Arguments.of("leading returns and year to date", new LeadingPeriodsReqValidator(), periodCommand(YTD)),
        Arguments.of("leading returns and since inception", new LeadingPeriodsReqValidator(), periodCommand(SI)),
        Arguments.of("leading returns and a request-defined window", new LeadingPeriodsReqValidator(),
            periodCommand(ONE_YR, CIPSD)),
        Arguments.of("risk metrics and half a year", new TwelveMonthMinimumPeriodsReqValidator(),
            periodCommand(SIX_MTH)),
        Arguments.of("risk metrics and year to date", new TwelveMonthMinimumPeriodsReqValidator(),
            periodCommand(ONE_YR, YTD)),
        Arguments.of("rolling returns and year to date", new RollingFixedLengthPeriodsReqValidator(),
            rollingCommand(ONE_YR, YTD)),
        Arguments.of("rolling correlation and a request-defined window", new RollingFixedLengthPeriodsReqValidator(),
            rollingCommand(CIPSD)),
        Arguments.of("rolling statistics and half a year", new RollingTwelveMonthMinimumPeriodsReqValidator(),
            rollingCommand(SIX_MTH)),
        Arguments.of("rolling statistics and since inception", new RollingTwelveMonthMinimumPeriodsReqValidator(),
            rollingCommand(ONE_YR, SI)));
  }

  private static List<CalculationMetric> claimedBy(final List<RequestValidator> validators) {
    return validators.stream().flatMap(validator -> validator.supportedMetrics().stream()).toList();
  }

  private static CalculationCommand periodCommand(final TimePeriod... periods) {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(new LinkedHashSet<>(List.of(periods)));
    return command;
  }

  private static CalculationCommand rollingCommand(final TimePeriod... windows) {
    RollingCalculationCommand command = new RollingCalculationCommand();
    command.setRollingPeriods(new LinkedHashSet<>(List.of(windows)));
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
