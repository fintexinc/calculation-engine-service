package ca.tangerine.pce.e2e;

import ca.tangerine.pce.PortfolioCalculationEngineApplication;
import ca.tangerine.pce.cache.fx.CachingFxRatesFetcher;
import ca.tangerine.pce.cache.fx.FxRatesCache;
import ca.tangerine.pce.model.domain.CurrencyExchangePair;
import ca.tangerine.pce.model.domain.calculation.DateRange;
import ca.tangerine.pce.port.webclient.boc.FxRatesFetcher;
import ca.tangerine.pce.webclient.boc.fetcher.BocFxRatesFetcher;
import ca.tangerine.wm.commons.domain.currency.Currency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockWebServer;

/**
 * Verifies the full Spring wiring when {@code cache.data.fx-rates.enabled=false}: no cache-adapter bean is registered,
 * the {@link FxRatesFetcher} resolved from the context is the raw {@link BocFxRatesFetcher}, and every fetch hits the
 * Bank of Canada endpoint (no caching between calls).
 */
@Tag("e2e")
@ActiveProfiles("test")
@SpringBootTest(classes = PortfolioCalculationEngineApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
    "cache.data.fx-rates.enabled=false"
})
class FxRatesCachingDisabledE2ETest {

  private static MockWebServer bocMockServer;

  @Autowired
  private FxRatesFetcher fxRatesFetcher;

  @Autowired
  private ApplicationContext applicationContext;

  @BeforeAll
  static void startBocMockServer() throws IOException {
    bocMockServer = new MockWebServer();
    bocMockServer.setDispatcher(BocMockResponses.dailyUsdCadDispatcher());
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
  void shouldWireRawBocFetcher_whenCachingDisabled() {
    assertThat(fxRatesFetcher).isInstanceOf(BocFxRatesFetcher.class);
    assertThat(fxRatesFetcher).isNotInstanceOf(CachingFxRatesFetcher.class);
    assertThat(applicationContext.getBeansOfType(FxRatesCache.class)).isEmpty();
    assertThat(applicationContext.getBeansOfType(CachingFxRatesFetcher.class)).isEmpty();
  }

  @Test
  void shouldHitBocOnEveryCall_whenCachingDisabled() {
    CurrencyExchangePair pair = new CurrencyExchangePair(Currency.USD, Currency.CAD);
    DateRange firstRange = new DateRange(LocalDate.parse("2022-01-01"), LocalDate.parse("2022-12-31"));
    DateRange subRangeInside = new DateRange(LocalDate.parse("2022-06-01"), LocalDate.parse("2022-07-31"));
    int baseline = bocMockServer.getRequestCount();

    fxRatesFetcher.fetch(pair, firstRange);
    int afterFirst = bocMockServer.getRequestCount();
    fxRatesFetcher.fetch(pair, subRangeInside);
    int afterSecond = bocMockServer.getRequestCount();

    assertThat(afterFirst).isEqualTo(baseline + 1);
    assertThat(afterSecond).isEqualTo(afterFirst + 1);
  }
}
