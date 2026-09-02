package ca.tangerine.pce.application.config;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.enumeration.SupportedPeriods;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.CIPSD;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.SI;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.YTD;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * Reports an unusable {@link TimePeriod} in a request as a bad request naming the offending value and listing what
 * would have been accepted, instead of a generic unreadable-body error.
 *
 * <p>
 * {@code TimePeriod} lives in the shared commons library and so cannot throw this service's {@link ErrorCode} — it
 * raises {@link IllegalArgumentException}, which Jackson wraps and {@code GlobalExceptionHandler} can only report as
 * {@code BAD_INPUT} because there is no {@code BasePceException} in the cause chain to unwrap. Since deserialization
 * fails before any request validator runs, the message has to be produced here or not at all.
 *
 * <p>
 * Spring Boot registers every {@code Module} bean with the auto-configured {@code ObjectMapper}, so declaring this as a
 * component is all the wiring it needs. Serialization is untouched: a period still goes out as its constant name, the
 * form Market Investment Catalogue already uses.
 */
@Component
public class TimePeriodJacksonModule extends SimpleModule {

  public TimePeriodJacksonModule() {
    addDeserializer(TimePeriod.class, new TimePeriodDeserializer());
  }

  private static final class TimePeriodDeserializer extends ValueDeserializer<TimePeriod> {

    @Override
    public TimePeriod deserialize(final JsonParser parser, final DeserializationContext context) {
      String raw = parser.getValueAsString();
      try {
        return TimePeriod.fromJson(raw);
      } catch (IllegalArgumentException exception) {
        throw ErrorCode.TIME_INTERVAL_PERIOD_NOT_SUPPORTED.toValidationException(raw, allMetrics(), supported());
      }
    }

    /**
     * Every metric the service exposes. Deserialization fails before the request is dispatched to a metric, and a token
     * that is not a {@link TimePeriod} at all is unusable for all of them, so the message names them all rather than
     * implying the period would have worked somewhere else.
     */
    private static String allMetrics() {
      return Arrays.stream(CalculationMetric.values())
          .map(CalculationMetric::getValue)
          .collect(Collectors.joining(", "));
    }

    /**
     * Every period the service declares, so a caller that sent {@code 18} can see that {@code ONE_YR} and
     * {@code TWO_YR} exist. Listed by name because the name is the wire form, fixed lengths in ascending order of
     * length, then the data-defined ones.
     */
    private static String supported() {
      Stream<TimePeriod> byLength = SupportedPeriods.FIXED_LENGTH.stream()
          .sorted(Comparator.comparingInt(TimePeriod::getMonths));
      return Stream.concat(byLength, Stream.of(YTD, SI, CIPSD))
          .map(Enum::name)
          .collect(Collectors.joining(", "));
    }
  }
}
