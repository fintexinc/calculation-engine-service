package ca.tangerine.pce.e2e;

import ca.tangerine.pce.PortfolioCalculationEngineApplication;
import ca.tangerine.pce.cache.fx.CachingFxRatesFetcher;
import ca.tangerine.pce.cache.fx.FxRatesCache;
import ca.tangerine.pce.model.domain.CurrencyExchangePair;
import ca.tangerine.pce.model.domain.calculation.DateRange;
import ca.tangerine.pce.port.webclient.boc.FxRatesFetcher;
import ca.tangerine.wm.commons.domain.currency.Currency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockWebServer;

/**
 * Verifies the full Spring wiring when {@code cache.data.fx-rates.enabled=true}: the {@link FxRatesFetcher} primary
 * bean is the cache proxy, and repeated sub-range queries hit Bank of Canada only once. Bank of Canada is mocked via
 * {@link MockWebServer} and request counts are asserted to confirm cache hits vs misses.
 */
@Tag("e2e")
@ActiveProfiles("test")
@SpringBootTest(classes = PortfolioCalculationEngineApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
    "cache.data.fx-rates.enabled=true"
})
class FxRatesCachingEnabledE2ETest {

  private static MockWebServer bocMockServer;

  @Autowired
  private FxRatesFetcher fxRatesFetcher;

  @Autowired
  private FxRatesCache fxRatesCache;

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
  void shouldWireCachingProxy_whenCachingEnabled() {
    assertThat(fxRatesFetcher).isInstanceOf(CachingFxRatesFetcher.class);
    assertThat(fxRatesCache).isNotNull();
  }

  @Test
  void shouldHitBocOnce_whenSubRangeAlreadyCoveredByPreviousFetch() {
    CurrencyExchangePair pair = new CurrencyExchangePair(Currency.USD, Currency.CAD);
    DateRange firstRange = new DateRange(LocalDate.parse("2024-01-01"), LocalDate.parse("2024-12-31"));
    DateRange subRangeInside = new DateRange(LocalDate.parse("2024-06-01"), LocalDate.parse("2024-07-31"));
    int baseline = bocMockServer.getRequestCount();

    NavigableMap<LocalDate, BigDecimal> firstRates = fxRatesFetcher.fetch(pair, firstRange);
    int afterFirst = bocMockServer.getRequestCount();

    NavigableMap<LocalDate, BigDecimal> subRates = fxRatesFetcher.fetch(pair, subRangeInside);
    int afterSecond = bocMockServer.getRequestCount();

    assertThat(firstRates).isNotEmpty();
    assertThat(afterFirst).isEqualTo(baseline + 1);
    assertThat(afterSecond).isEqualTo(afterFirst);
    assertThat(subRates).isNotEmpty();
    assertThat(subRates.firstKey()).isAfterOrEqualTo(subRangeInside.start());
    assertThat(subRates.lastKey()).isBeforeOrEqualTo(subRangeInside.end());
  }

  @Test
  void shouldFetchOnlyMissingTail_whenCacheIsPartiallyCovered() {
    CurrencyExchangePair pair = new CurrencyExchangePair(Currency.USD, Currency.CAD);
    DateRange cachedRange = new DateRange(LocalDate.parse("2023-01-01"), LocalDate.parse("2023-06-30"));
    DateRange extendedRange = new DateRange(LocalDate.parse("2023-01-01"), LocalDate.parse("2023-12-31"));
    int baseline = bocMockServer.getRequestCount();

    fxRatesFetcher.fetch(pair, cachedRange);
    int afterFirst = bocMockServer.getRequestCount();
    fxRatesFetcher.fetch(pair, extendedRange);
    int afterSecond = bocMockServer.getRequestCount();

    assertThat(afterFirst).isEqualTo(baseline + 1);
    assertThat(afterSecond).isEqualTo(afterFirst + 1);
  }
}
