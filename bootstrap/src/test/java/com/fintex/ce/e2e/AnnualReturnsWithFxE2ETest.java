package com.fintex.ce.e2e;

import com.fintex.ce.adapter.webclient.boc.client.BankOfCanadaProperties;
import com.fintex.ce.adapter.webclient.boc.dto.BankOfCanadaFxRateResponse;
import com.fintex.ce.adapter.webclient.boc.dto.BankOfCanadaFxRateResponse.Observation;
import com.fintex.ce.model.domain.result.KeyValueResult;
import com.fintex.ce.model.domain.result.returns.AnnualReturnResult;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * FX path for annual-returns: USD ETFs are requested in CAD and monthly returns are converted via BoC rates before the
 * per-calendar-year compounding.
 */
@Tag("e2e")
class AnnualReturnsWithFxE2ETest extends AbstractAnnualReturnsE2ETest {

  private static final String FX_USD_CAD = "FXUSDCAD";

  // Month-ends of the priced year plus the preceding December, so each month's converted return has both the current
  // and prior-month rate available.
  private static final List<String> FX_MONTH_ENDS = List.of(
      "2023-12-31", "2024-01-31", "2024-02-29", "2024-03-31", "2024-04-30", "2024-05-31", "2024-06-30",
      "2024-07-31", "2024-08-31", "2024-09-30", "2024-10-31", "2024-11-30", "2024-12-31");

  private static final MockWebServer bocMockServer = MockWebServers.started();

  @AfterAll
  static void shutdownBocMockServer() throws IOException {
    bocMockServer.shutdown();
  }

  @DynamicPropertySource
  static void registerBocBaseUrl(DynamicPropertyRegistry registry) {
    registry.add(BankOfCanadaProperties.BASE_URL_PROPERTY,
        () -> MockWebServers.baseUrl(bocMockServer));
  }

  @BeforeEach
  void resetBocDispatcher() {
    bocMockServer.setDispatcher(usdCadDispatcher(FX_MONTH_ENDS));
  }

  @Test
  void shouldConvertUsdHoldingsToCad_whenTargetCurrencyDiffers() {
    enqueueSmsMockResponse(writeJson(List.of(
        securityAttributeResult(VTI, fullYear2024Returns()),
        securityAttributeResult(SPY, fullYear2024Returns()))));

    HttpResponse response = postCalculation(writeJson(commandFor(Currency.CAD, List.of(
        holding(VTI, FinancialInstrumentType.ETF, Country.USA, "45234.67"),
        holding(SPY, FinancialInstrumentType.ETF, Country.USA, "18765.43")))));

    assertThat(response.status().value()).isEqualTo(200);
    AnnualReturnResult<?> result = readJson(response.responseBody(), AnnualReturnResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getPerformanceStartDate()).isEqualTo(LocalDate.of(2024, 1, 31));
    assertThat(result.getPerformanceEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
    assertThat(result.getAnnualReturns()).hasSize(1);
    KeyValueResult<?> entry = result.getAnnualReturns().getFirst();
    assertThat(entry.key()).isEqualTo(2024);
    assertThat(entry.value()).isEqualByComparingTo(new BigDecimal("0.1048520232"));
  }

  private static Dispatcher usdCadDispatcher(List<String> monthEnds) {
    BankOfCanadaFxRateResponse response = new BankOfCanadaFxRateResponse();
    response.setObservations(IntStream.range(0, monthEnds.size())
        .mapToObj(i -> bocObservation(monthEnds.get(i), BigDecimal.valueOf(130L + i, 2).toPlainString()))
        .toList());
    String body = writeJson(response);
    return new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) {
        return new MockResponse()
            .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .setBody(body);
      }
    };
  }

  private static Observation bocObservation(String date, String rate) {
    Observation observation = new Observation();
    observation.setDate(date);
    observation.setDynamicProperty(FX_USD_CAD, Map.of("v", rate));
    return observation;
  }
}
