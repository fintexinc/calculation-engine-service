package ca.tangerine.pce.application.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.ValidationException;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

class TimePeriodJacksonModuleTest {

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new TimePeriodJacksonModule());

  @ParameterizedTest
  @CsvSource({
      "\"ONE_YR\", ONE_YR",
      "\"12\", ONE_YR",
      "\"YTD\", YTD",
      "\"SI\", SI",
      "\"CIPSD\", CIPSD"
  })
  void shouldDeserialize_whenTokenIsAKnownPeriodOrItsLengthInMonths(String json, TimePeriod expected)
      throws Exception {
    assertThat(objectMapper.readValue(json, TimePeriod.class)).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(strings = {"18", "0", "-3", "NOT_A_PERIOD", "one year", ""})
  void shouldThrowValidationExceptionCarryingTheErrorCode_whenTokenIsNotAPeriod(String raw) {
    assertThatThrownBy(() -> objectMapper.readValue('"' + raw + '"', TimePeriod.class))
        .isInstanceOf(ValidationException.class)
        .satisfies(thrown -> assertThat(((ValidationException) thrown).getErrorCode())
            .isEqualTo(ErrorCode.TIME_INTERVAL_PERIOD_NOT_SUPPORTED));
  }

  /**
   * Pins the message arity: the pattern carries three tokens, so a call site passing fewer would fail inside
   * {@code String.format} and turn a documented bad request into an unhandled server error.
   */
  @Test
  void shouldNameTheTokenEveryMetricAndEveryPeriod_whenTokenIsNotAPeriod() {
    assertThatThrownBy(() -> objectMapper.readValue("\"18\"", TimePeriod.class))
        .isInstanceOf(ValidationException.class)
        .satisfies(thrown -> {
          String message = thrown.getMessage();
          assertThat(message).startsWith("Time interval period '18' is not supported for metrics ");
          assertThat(message).contains(". Supported periods: ");
          assertThat(message).doesNotContain("%s");
          assertThat(metricsSection(message).split(", "))
              .as("a token that is no period at all is unusable for every metric")
              .hasSize(CalculationMetric.values().length)
              .contains(CalculationMetric.TRAILING_TOTAL_RETURNS.getValue(),
                  CalculationMetric.SHARPE_RATIO.getValue());
          assertThat(periodsSection(message).split(", "))
              .contains(TimePeriod.ONE_YR.name(), TimePeriod.TWO_YR.name(),
                  TimePeriod.YTD.name(), TimePeriod.SI.name(), TimePeriod.CIPSD.name())
              .doesNotHaveDuplicates();
        });
  }

  @Test
  void shouldSerializeAsTheConstantName_whenPeriodIsWritten() throws Exception {
    assertThat(objectMapper.writeValueAsString(TimePeriod.ONE_YR)).isEqualTo("\"ONE_YR\"");
    assertThat(objectMapper.writeValueAsString(Set.of(TimePeriod.YTD))).isEqualTo("[\"YTD\"]");
  }

  private static String metricsSection(String message) {
    int from = message.indexOf("for metrics ") + "for metrics ".length();
    return message.substring(from, message.indexOf(". Supported periods: "));
  }

  private static String periodsSection(String message) {
    return message.substring(message.indexOf("Supported periods: ") + "Supported periods: ".length());
  }
}
