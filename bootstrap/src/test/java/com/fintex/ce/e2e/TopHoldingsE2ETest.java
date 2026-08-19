package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.holding.TopCommonHoldingData;
import com.fintex.ce.model.domain.result.holding.TopCommonHoldingsResult;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.ce.model.dto.command.TopCommonHoldingsCommand;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.enumeration.LanguageCode;
import com.fintex.wm.commons.domain.holding.Holdings;
import com.fintex.wm.commons.domain.holding.SecurityHolding;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.value.MultilingualString;

import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
class TopHoldingsE2ETest extends AbstractPortfolioCalculationE2ETest {

  @Override
  protected String metricPath() {
    return "top-common-holdings";
  }

  @Override
  protected String requestBodyForMicUnavailableScenario() {
    return requestBodyForPositiveMicScenario();
  }

  @Override
  protected String requestBodyForPositiveMicScenario() {
    var command = new TopCommonHoldingsCommand();
    command.setMetric(CalculationMetric.TOP_COMMON_HOLDINGS);
    command.setNumOfTopCommonHoldings(5);
    command.setHoldings(List.of(
        holding(new SecurityIdentifier("AAA_PARENT", FiIdentifierType.TICKER), 60_000),
        holding(new SecurityIdentifier("BBB_PARENT", FiIdentifierType.FUNDSERV), 40_000)));
    command.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return writeJson(command);
  }

  @Override
  protected String micPositiveResponseBody() {
    var response = List.of(
        securityAttributeResult(
            new SecurityIdentifier("AAA_PARENT", FiIdentifierType.TICKER),
            holdings(
                allocation("Alpha Corp", "E", "0.20", "50000"),
                allocation("Bravo Corp", "E", "0.18", "45000"),
                allocation("Charlie Corp", "E", "0.16", "40000"),
                allocation("Delta Corp", "E", "0.14", "35000"),
                allocation("Echo Corp", "E", "0.12", "30000"),
                allocation("Foxtrot Corp", "E", "0.10", "25000"))),
        securityAttributeResult(
            new SecurityIdentifier("BBB_PARENT", FiIdentifierType.FUNDSERV),
            holdings(
                allocation("Alpha Corp", "E", "0.25", "60000"),
                allocation("Bravo Corp", "E", "0.20", "48000"),
                allocation("Golf Corp", "E", "0.15", "36000"),
                allocation("Hotel Corp", "E", "0.12", "28800"),
                allocation("India Corp", "E", "0.10", "24000"),
                allocation("Juliet Corp", "E", "0.08", "19200"),
                allocation("Kilo Corp", "E", "0.06", "14400"),
                allocation("Lima Corp", "E", "0.04", "9600"))));

    return writeJson(response);
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    var command = new ReturnCommand();
    command.setMetric(CalculationMetric.GROWTH_OF_10K);
    command.setHoldings(List.of(holding(new SecurityIdentifier("AAA_PARENT", FiIdentifierType.TICKER), 50_000)));
    command.setCurrency(Currency.CAD);
    return writeJson(command);
  }

  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    TopCommonHoldingsResult result = readJson(responseBody, TopCommonHoldingsResult.class);

    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getCommonHoldings()).hasSize(5);

    List<BigDecimal> allocations = result.getCommonHoldings().stream()
        .map(TopCommonHoldingData::getAllocation)
        .toList();
    assertThat(allocations).isSortedAccordingTo(Comparator.reverseOrder());

    List<String> names = result.getCommonHoldings().stream()
        .map(TopCommonHoldingData::getName)
        .toList();
    assertThat(names)
        .containsExactlyInAnyOrder("Alpha Corp", "Bravo Corp", "Charlie Corp", "Delta Corp", "Echo Corp");

    Map<String, TopCommonHoldingData> byName = result.getCommonHoldings().stream()
        .collect(Collectors.toMap(TopCommonHoldingData::getName, h -> h));
    assertThat(byName.get("Alpha Corp").getNumOfFunds()).isEqualTo(2);
    assertThat(byName.get("Alpha Corp").getParentHolding()).hasSize(2);
    assertThat(byName.get("Bravo Corp").getNumOfFunds()).isEqualTo(2);
    assertThat(byName.get("Bravo Corp").getParentHolding()).hasSize(2);
    assertThat(byName.get("Charlie Corp").getNumOfFunds()).isEqualTo(1);
    assertThat(byName.get("Charlie Corp").getParentHolding()).hasSize(1);
    assertThat(byName.get("Delta Corp").getNumOfFunds()).isEqualTo(1);
    assertThat(byName.get("Delta Corp").getParentHolding()).hasSize(1);
    assertThat(byName.get("Echo Corp").getNumOfFunds()).isEqualTo(1);
    assertThat(byName.get("Echo Corp").getParentHolding()).hasSize(1);
  }

  @Test
  void shouldAggregateSharedHoldingBeyondTopTwentyFiveAcrossPortfolioHoldings() {
    SecurityIdentifier parentAId = new SecurityIdentifier("AAA_PARENT", FiIdentifierType.TICKER);
    SecurityIdentifier parentBId = new SecurityIdentifier("BBB_PARENT", FiIdentifierType.FUNDSERV);

    List<SecurityHolding> parentBHoldings = new ArrayList<>();

    // Use distinct, intentionally unordered weights so the expected result
    // order proves that holdings are sorted by calculated allocation rather
    // than preserving the MIC input order.
    String[] weights = {
        "18.0", "24.0", "15.0", "22.0", "20.0",
        "26.0", "17.0", "25.0", "19.0", "23.0",
        "21.0", "16.0", "14.0", "13.0", "12.0",
        "11.0", "10.0", "9.0", "8.0", "7.0",
        "6.0", "5.0", "4.0", "3.0", "2.0"
    };

    // Add 25 holdings with weights higher than Shared Corp,
    // placing Shared Corp outside Parent B's top 25.
    for (int i = 1; i <= 25; i++) {
      parentBHoldings.add(allocation(
          "Filler Corp " + i,
          "E",
          weights[i - 1],
          "10000"));
    }

    // Shared Corp is the 26th holding in Parent B.
    parentBHoldings.add(allocation(
        "Shared Corp",
        "E",
        "1.0",
        "5000"));

    var micResponse = List.of(
        securityAttributeResult(
            parentAId,
            holdings(
                allocation(
                    "Shared Corp",
                    "E",
                    "20.0",
                    "50000"))),
        securityAttributeResult(
            parentBId,
            holdings(
                parentBHoldings.toArray(SecurityHolding[]::new))));

    enqueueMicMockResponse(writeJson(micResponse));

    var command = new TopCommonHoldingsCommand();
    command.setMetric(CalculationMetric.TOP_COMMON_HOLDINGS);
    command.setNumOfTopCommonHoldings(10);
    command.setHoldings(List.of(
        holding(parentAId, 60_000),
        holding(parentBId, 40_000)));
    command.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    HttpResponse response = postCalculation(writeJson(command));

    assertThat(response.status().value())
        .isEqualTo(HttpStatus.OK.value());

    TopCommonHoldingsResult result = readJson(response.responseBody(), TopCommonHoldingsResult.class);

    assertThat(result.getWarnings()).isEmpty();

    // Verify the whole Top 10 result is ordered by calculated allocation
    // rather than by the MIC input order.
    assertThat(result.getCommonHoldings())
        .extracting(TopCommonHoldingData::getName)
        .containsExactly(
            "Shared Corp",
            "Filler Corp 6",
            "Filler Corp 8",
            "Filler Corp 2",
            "Filler Corp 10",
            "Filler Corp 4",
            "Filler Corp 11",
            "Filler Corp 5",
            "Filler Corp 9",
            "Filler Corp 1");

    TopCommonHoldingData sharedHolding = result.getCommonHoldings().stream()
        .filter(holding -> "Shared Corp".equals(holding.getName()))
        .findFirst()
        .orElseThrow();

    // Parent A: 60% × 20% = 12%
    // Parent B: 40% × 1% = 0.4%
    // Total: 12.4% = 0.124
    assertThat(sharedHolding.getAllocation())
        .isEqualByComparingTo("0.124");

    assertThat(sharedHolding.getNumOfFunds())
        .isEqualTo(2);

    assertThat(sharedHolding.getParentHolding())
        .hasSize(2);
  }

  private static PortfolioHolding holding(SecurityIdentifier securityIdentifier, int value) {
    return new PortfolioHolding(
        BigDecimal.valueOf(value),
        FinancialInstrumentType.ETF,
        Country.CANADA,
        securityIdentifier);
  }

  private static Holdings holdings(SecurityHolding... allocations) {
    var h = new Holdings();
    h.setAllocation(List.of(allocations));
    h.setCurrency(Currency.CAD);
    h.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return h;
  }

  private static SecurityHolding allocation(
      String companyName,
      String type,
      String weighting,
      String marketValue) {
    var a = new SecurityHolding();
    a.setCompanyName(companyName);
    a.setName(List.of(new MultilingualString(LanguageCode.EN, companyName)));
    a.setType(type);
    a.setWeighting(new BigDecimal(weighting));
    a.setMarketValue(new BigDecimal(marketValue));
    return a;
  }

}
