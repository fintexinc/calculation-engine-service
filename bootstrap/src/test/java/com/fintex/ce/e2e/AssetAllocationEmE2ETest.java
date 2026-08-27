package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.AssetAllocationEMResult;
import com.fintex.ce.model.domain.result.allocation.AssetAllocationResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.wm.commons.domain.allocation.AssetAllocation;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.allocation.AssetAllocationValue;
import com.fintex.wm.commons.domain.allocation.AssetAllocationWithCurrency;
import com.fintex.wm.commons.domain.allocation.SecurityRegion;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static com.fintex.ce.e2e.BreakdownDistributions.assertDistribution;
import static com.fintex.ce.e2e.BreakdownDistributions.assertTotalsToOne;
import static com.fintex.ce.e2e.SmsAttributeResponses.attributeResult;
import static com.fintex.ce.e2e.SmsAttributeResponses.compositeDispatcher;
import static com.fintex.ce.e2e.SmsAttributeResponses.currencyDatapoint;
import static com.fintex.ce.e2e.SmsAttributeResponses.geographyRow;
import static com.fintex.ce.e2e.SmsAttributeResponses.morningstarOnly;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.cash;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.etfCa;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.fundCa;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.gic;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.stockCa;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end coverage for the {@code /asset-allocations-em} endpoint. It is the asset-allocation breakdown with one
 * difference — emerging-market equities keep a bucket of their own instead of being folded into international equities
 * — so a test of this endpoint that did not compare it against {@code /asset-allocations} would distinguish nothing.
 * {@link #shouldKeepEmergingMarketsSeparate_whereAssetAllocationsFoldsThemIntoInternational()} sends the same portfolio
 * to both and asserts they disagree on exactly that.
 *
 * <p>
 * These buckets are absolute portfolio proportions — cash, GICs and every asset class together account for the whole
 * portfolio — so unlike the exposure metrics they are reported without a final rescale to 100%. The fixtures are
 * therefore built to sum to one by themselves, and asserting the total is a real check rather than a tautology.
 */
@Tag("e2e")
class AssetAllocationEmE2ETest extends AbstractPortfolioCalculationE2ETest {

  private static final String BALANCED_FUND = "F00000BAL1";
  private static final String EMERGING_MARKETS_ETF = "XEM";
  private static final String STOCK_TICKER = "AAPL";
  private static final String STOCK_EXCHANGE = "NASDAQ";

  @Override
  protected String metricPath() {
    return CalculationMetric.ASSET_ALLOCATIONS_EM.getValue();
  }

  @Override
  protected String requestBodyForSmsUnavailableScenario() {
    return writeJson(allocationsCommand(CalculationMetric.ASSET_ALLOCATIONS_EM,
        fundCa(BALANCED_FUND, 50_000), etfCa(EMERGING_MARKETS_ETF, 50_000)));
  }

  /**
   * A portfolio shaped like a client's rather than the minimum the shared scenario needs: a balanced fund holding a
   * little of everything, an emerging-markets ETF, an individual stock resolved through its geography, cash, and a GIC
   * beyond the one-year line so it buckets as fixed income rather than as cash. Every branch of the metric runs in this
   * one request.
   */
  @Override
  protected String requestBodyForPositiveSmsScenario() {
    return writeJson(allocationsCommand(CalculationMetric.ASSET_ALLOCATIONS_EM, positivePortfolio()));
  }

  @Override
  protected String smsPositiveResponseBody() {
    return writeJson(positiveScenarioRows());
  }

  /**
   * The shared positive scenario enqueues a single response, which suffices for one holding; this portfolio holds five
   * across three identifier types, and how many attribute calls the fetcher batches them into is an implementation
   * detail. Answering every {@code /attributes} call from a dispatcher keeps the test about the metric rather than
   * about the batching.
   */
  @Override
  protected void enqueueForPositiveSmsScenario() {
    smsMockServer.setDispatcher(compositeDispatcher(positiveScenarioRows()));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(CalculationMetric.SHARPE_RATIO);
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(fundCa(BALANCED_FUND, 50_000)));
    return writeJson(command);
  }

  /**
   * The full expected payload, derived by hand from the request above. The five holdings total 100 000, so they weigh
   * 0.4 / 0.2 / 0.2 / 0.1 / 0.1. US_EQUITIES is 0.4·0.40 + 0.2 (the stock's whole weight, from its USA region) = 0.36;
   * EM_EQUITIES is 0.4·0.20 + 0.2·1.00 = 0.28; INTERNATIONAL_EQUITIES is 0.4·0.20 = 0.08; FIXED_INCOME is 0.4·0.20 +
   * 0.1 (the three-year GIC) = 0.18; CASH is the cash holding's 0.10.
   */
  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    AssetAllocationEMResult result = readJson(responseBody, AssetAllocationEMResult.class);

    assertThat(result.getWarnings()).isEmpty();
    assertDistribution(result.getAssetAllocationEmergingMarkets(), AssetAllocationRegionType.class, Map.of(
        AssetAllocationRegionType.US_EQUITIES, "0.36",
        AssetAllocationRegionType.EM_EQUITIES, "0.28",
        AssetAllocationRegionType.INTERNATIONAL_EQUITIES, "0.08",
        AssetAllocationRegionType.FIXED_INCOME, "0.18",
        AssetAllocationRegionType.CASH, "0.10"));
    assertTotalsToOne(result.getAssetAllocationEmergingMarkets());
  }

  /**
   * The one behaviour that makes this a separate endpoint, asserted the only way it can be: the same portfolio and the
   * same Security Master data sent to both endpoints, which must agree everywhere except emerging markets. Here that is
   * 28% of the portfolio — reported on its own by this metric, and added to international equities by
   * {@code /asset-allocations}, which drops the bucket from its payload entirely rather than reporting it as zero.
   *
   * <p>
   * Asserting only this endpoint would pass just as well if it were wired to the collapsing service, since a portfolio
   * with no emerging-market exposure looks identical either way.
   */
  @Test
  void shouldKeepEmergingMarketsSeparate_whereAssetAllocationsFoldsThemIntoInternational() {
    smsMockServer.setDispatcher(compositeDispatcher(positiveScenarioRows()));

    var emergingMarketsResponse = postCalculation(
        writeJson(allocationsCommand(CalculationMetric.ASSET_ALLOCATIONS_EM, positivePortfolio())));
    var collapsedResponse = post(CalculationMetric.ASSET_ALLOCATIONS.getValue(),
        writeJson(allocationsCommand(CalculationMetric.ASSET_ALLOCATIONS, positivePortfolio())));

    assertThat(emergingMarketsResponse.status().value()).isEqualTo(HttpStatus.OK.value());
    assertThat(collapsedResponse.status().value()).isEqualTo(HttpStatus.OK.value());
    Map<AssetAllocationRegionType, BigDecimal> separate = readJson(emergingMarketsResponse.responseBody(),
        AssetAllocationEMResult.class).getAssetAllocationEmergingMarkets();
    Map<AssetAllocationRegionType, BigDecimal> collapsed = readJson(collapsedResponse.responseBody(),
        AssetAllocationResult.class).getAssetAllocation();

    assertThat(separate.get(AssetAllocationRegionType.EM_EQUITIES)).isEqualByComparingTo("0.28");
    assertThat(separate.get(AssetAllocationRegionType.INTERNATIONAL_EQUITIES)).isEqualByComparingTo("0.08");
    assertThat(collapsed).doesNotContainKey(AssetAllocationRegionType.EM_EQUITIES);
    assertThat(collapsed.get(AssetAllocationRegionType.INTERNATIONAL_EQUITIES)).isEqualByComparingTo("0.36");
    for (AssetAllocationRegionType bucket : AssetAllocationRegionType.values()) {
      if (bucket == AssetAllocationRegionType.EM_EQUITIES
          || bucket == AssetAllocationRegionType.INTERNATIONAL_EQUITIES) {
        continue;
      }
      assertThat(collapsed.get(bucket))
          .as("only the emerging-markets bucket and the one absorbing it may differ, but %s did", bucket)
          .isEqualByComparingTo(separate.get(bucket));
    }
  }

  /**
   * Posts to a metric endpoint other than this class's own, which {@code postCalculation} cannot do: the whole point of
   * the comparison above is to reach the sibling metric with the same request.
   */
  private HttpResponse post(String metricName, String body) {
    var exchangeResult = webTestClient.post()
        .uri("/api/v1/portfolio/calculations/" + metricName)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .exchange()
        .expectBody(String.class)
        .returnResult();
    return new HttpResponse(exchangeResult.getStatus(), exchangeResult.getResponseBody());
  }

  private static PortfolioHolding[] positivePortfolio() {
    return new PortfolioHolding[] {
        fundCa(BALANCED_FUND, 40_000),
        etfCa(EMERGING_MARKETS_ETF, 20_000),
        stockCa(STOCK_TICKER, STOCK_EXCHANGE, 20_000),
        cash(Currency.CAD, 10_000),
        gic(new SecurityIdentifier("GIC-RBC-3Y", FiIdentifierType.TICKER), Currency.CAD, BigDecimal.valueOf(10_000),
            BigDecimal.valueOf(1095))};
  }

  private static Map<CompositeSecurityAttribute, List<? extends SecurityAttributeResult<?>>> positiveScenarioRows() {
    return Map.of(
        CompositeSecurityAttribute.ASSET_ALLOCATION, List.of(
            allocationRow(BALANCED_FUND, FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
                allocationValue(AssetAllocationRegionType.US_EQUITIES, "0.40"),
                allocationValue(AssetAllocationRegionType.EM_EQUITIES, "0.20"),
                allocationValue(AssetAllocationRegionType.INTERNATIONAL_EQUITIES, "0.20"),
                allocationValue(AssetAllocationRegionType.FIXED_INCOME, "0.20")),
            allocationRow(EMERGING_MARKETS_ETF, FiIdentifierType.TICKER, Currency.CAD,
                allocationValue(AssetAllocationRegionType.EM_EQUITIES, "1.00"))),
        CompositeSecurityAttribute.GEOGRAPHY, List.of(
            geographyRow(BALANCED_FUND, FiIdentifierType.MORNINGSTAR_ID, null, null, Currency.CAD),
            geographyRow(EMERGING_MARKETS_ETF, FiIdentifierType.TICKER, null, null, Currency.CAD),
            geographyRow(STOCK_TICKER, FiIdentifierType.TICKER_MIC, null, SecurityRegion.USA, Currency.CAD)));
  }

  private static PortfolioHoldingsCommand allocationsCommand(CalculationMetric metric, PortfolioHolding... holdings) {
    return PortfolioHoldingsCommand.builder()
        .metric(metric)
        .holdings(List.of(holdings))
        .dataProviders(morningstarOnly())
        .build();
  }

  private static SecurityAttributeResult<AssetAllocationWithCurrency> allocationRow(String id,
      FiIdentifierType idType, Currency currency, AssetAllocationValue... values) {
    AssetAllocation allocation = new AssetAllocation();
    allocation.setAllocations(new ArrayList<>(List.of(values)));
    allocation.setDataProviders(morningstarOnly());

    AssetAllocationWithCurrency row = new AssetAllocationWithCurrency();
    row.setAssetAllocation(allocation);
    row.setCurrency(currencyDatapoint(currency));
    row.setDataProviders(morningstarOnly());
    return attributeResult(id, idType, row);
  }

  private static AssetAllocationValue allocationValue(AssetAllocationRegionType type, String value) {
    return new AssetAllocationValue(type, new BigDecimal(value), new TreeSet<>());
  }
}
