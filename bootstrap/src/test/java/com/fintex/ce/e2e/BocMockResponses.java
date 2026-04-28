package com.fintex.ce.e2e;

import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Shared helpers for stubbing Bank of Canada responses in bootstrap-level integration tests. Produces a dispatcher that
 * answers every request with a synthetic daily USD/CAD observation per month covering 2020–2026, which is enough data
 * for any test range used by the FX caching E2E tests.
 */
@UtilityClass
final class BocMockResponses {

  static Dispatcher dailyUsdCadDispatcher() {
    String body = buildObservationsJson("FXUSDCAD",
        LocalDate.of(2020, Month.JANUARY, 1), LocalDate.of(2026, Month.DECEMBER, 1));
    return new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) {
        return new MockResponse()
            .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .setBody(body);
      }
    };
  }

  private static String buildObservationsJson(String seriesName, LocalDate from, LocalDate toInclusive) {
    List<String> observations = new ArrayList<>();
    LocalDate cursor = from;
    int counter = 0;
    while (!cursor.isAfter(toInclusive)) {
      String rate = String.format("1.%03d", 350 + (counter++ % 100));
      observations.add(String.format("{\"d\":\"%s\",\"%s\":{\"v\":\"%s\"}}", cursor, seriesName, rate));
      cursor = cursor.plusMonths(1);
    }
    return "{\"observations\":[" + String.join(",", observations) + "]}";
  }
}
