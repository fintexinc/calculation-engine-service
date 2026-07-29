package com.fintex.ce.application.config;

import com.fintex.ce.model.domain.enumeration.SupportedPeriods;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fintex.wm.commons.domain.enumeration.TimePeriod.CIPSD;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.SI;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.YTD;

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
 * form Security Master already uses.
 */
@Component
public class TimePeriodJacksonModule extends SimpleModule {

  public TimePeriodJacksonModule() {
    addDeserializer(TimePeriod.class, new TimePeriodDeserializer());
  }

  private static final class TimePeriodDeserializer extends JsonDeserializer<TimePeriod> {

    @Override
    public TimePeriod deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
      String raw = parser.getValueAsString();
      try {
        return TimePeriod.fromJson(raw);
      } catch (IllegalArgumentException exception) {
        throw ErrorCode.TIME_INTERVAL_PERIOD_NOT_SUPPORTED.toValidationException(raw, supported());
      }
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
