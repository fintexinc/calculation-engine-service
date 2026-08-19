package com.fintex.ce.e2e;

import com.fintex.ce.adapter.webclient.boc.dto.BankOfCanadaFxRateResponse;
import com.fintex.ce.adapter.webclient.boc.dto.BankOfCanadaFxRateResponse.Observation;
import com.fintex.ce.model.domain.result.returns.Growth10KResult;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * FX path for growth-of-10k: USD ETFs are requested in CAD and monthly returns are converted via BoC rates before
 * compounding.
 */
@Tag("e2e")
class Growth10kWithFxE2ETest extends AbstractGrowthOf10kE2ETest {

  private static final String FX_USD_CAD = "FXUSDCAD";

  private static MockWebServer bocMockServer;

  @BeforeAll
  static void startBocMockServer() throws IOException {
    bocMockServer = new MockWebServer();
    bocMockServer.setDispatcher(stepFunctionUsdCadDispatcher());
    bocMockServer.start();
  }

  @AfterAll
  static void shutdownBocMockServer() throws IOException {
    if (bocMockServer != null) {
      bocMockServer.shutdown();
      bocMockServer = null;
    }
  }

  @DynamicPropertySource
  static void registerBocBaseUrl(DynamicPropertyRegistry registry) {
    registry.add("external-services.bank-of-canada.base-url",
        () -> bocMockServer.url("/").toString().replaceAll("/$", ""));
  }

  @Test
  void shouldConvertUsdHoldingsToCad_whenTargetCurrencyDiffers() {
    enqueueSmsMockResponse(writeJson(List.of(
        securityAttributeResult(
            VTI,
            monthlyReturns(
                returns(
                    "2024-01-31", "5.0",
                    "2024-02-29", "-2.0"),
                DataProvider.MORNINGSTAR,
                "2024-02-29T00:00:00")),
        securityAttributeResult(
            SPY,
            monthlyReturns(
                returns(
                    "2024-01-31", "1.0",
                    "2024-02-29", "1.0"),
                DataProvider.MORNINGSTAR,
                "2024-02-29T00:00:00")))));

    HttpResponse response = postCalculation(writeJson(commandFor(Currency.CAD, List.of(
        holding(VTI, FinancialInstrumentType.ETF, Country.USA, "45234.67"),
        holding(SPY, FinancialInstrumentType.ETF, Country.USA, "18765.43")))));

    assertThat(response.status().value()).isEqualTo(200);
    Growth10KResult result = readJson(response.responseBody(), Growth10KResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getPerformanceStartDate()).isEqualTo(LocalDate.of(2023, 12, 31));
    assertThat(result.getPerformanceEndDate()).isEqualTo(LocalDate.of(2024, 2, 29));
    assertThat(result.getGrowth10k()).hasSize(3);
    assertGrowthPoint(result.getGrowth10k().get(0), "2023-12-31", "10000");
    assertGrowthPoint(result.getGrowth10k().get(1), "2024-01-31", "20765.4324915117");
    assertGrowthPoint(result.getGrowth10k().get(2), "2024-02-29", "20532.7824327645");
  }

  private static Dispatcher stepFunctionUsdCadDispatcher() {
    BankOfCanadaFxRateResponse response = new BankOfCanadaFxRateResponse();
    response.setObservations(List.of(
        bocObservation("2023-11-30", "1.0"),
        bocObservation("2023-12-31", "1.0"),
        bocObservation("2024-01-31", "2.0"),
        bocObservation("2024-02-29", "2.0")));
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
