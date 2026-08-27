package com.fintex.ce.e2e;

import org.springframework.http.MediaType;

import com.fasterxml.jackson.annotation.JsonValue;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Shared dispatchers for stubbing the Bank of Canada in bootstrap-level integration tests: a constant spot rate, or a
 * synthetic daily USD/CAD observation per month covering 2020–2026, which is enough data for any test range used by the
 * FX caching E2E tests.
 */
@UtilityClass
final class BocMockResponses {

  private static final String USD_CAD_SERIES = "FXUSDCAD";

  /**
   * Answers every request with a single USD/CAD observation dated today at {@code rate}. For a metric weighted on spot
   * rates — which are looked up for {@code LocalDate.now(UTC)} — one constant observation is what makes an expected
   * percentage exact instead of a function of the rate of the day.
   */
  static Dispatcher constantUsdCadRateDispatcher(String rate) {
    return constantBodyDispatcher(new Observations(
        List.of(new Observation(LocalDate.now(ZoneOffset.UTC), USD_CAD_SERIES, rate))));
  }

  static Dispatcher dailyUsdCadDispatcher() {
    return constantBodyDispatcher(monthlyObservations(USD_CAD_SERIES,
        LocalDate.of(2020, Month.JANUARY, 1), LocalDate.of(2026, Month.DECEMBER, 1)));
  }

  private static Dispatcher constantBodyDispatcher(Observations observations) {
    String body = AbstractPortfolioCalculationE2ETest.writeJson(observations);
    return new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) {
        return new MockResponse()
            .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .setBody(body);
      }
    };
  }

  private static Observations monthlyObservations(String series, LocalDate from, LocalDate toInclusive) {
    List<Observation> observations = new ArrayList<>();
    LocalDate cursor = from;
    int counter = 0;
    while (!cursor.isAfter(toInclusive)) {
      observations.add(new Observation(cursor, series, String.format("1.%03d", 350 + (counter++ % 100))));
      cursor = cursor.plusMonths(1);
    }
    return new Observations(observations);
  }

  private record Observations(List<Observation> observations) {
  }

  /**
   * One vendor observation, serialized with the shared {@code ObjectMapper} rather than concatenated, so a fixture can
   * only ever be syntactically valid JSON.
   *
   * <p>
   * It renders itself through {@link JsonValue} because Bank of Canada carries the series name as a JSON <em>key</em>
   * ({@code {"d": "...", "FXUSDCAD": {"v": "1.35"}}}), which fixed record components cannot express. The production
   * {@code BankOfCanadaFxRateResponse} reads that shape through a {@code @JsonAnySetter} with no getter counterpart —
   * it is a response DTO and deserializes only — so reusing it here would emit the mapped field name instead of the
   * series and stub a payload the client cannot parse.
   */
  private record Observation(LocalDate date, String series, String rate) {

    @JsonValue
    Map<String, Object> asJson() {
      Map<String, Object> json = new LinkedHashMap<>();
      json.put("d", date.toString());
      json.put(series, Map.of("v", rate));
      return json;
    }
  }
}
