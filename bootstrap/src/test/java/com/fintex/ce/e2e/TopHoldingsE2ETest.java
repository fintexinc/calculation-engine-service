package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.holding.TopCommonHoldingData;
import com.fintex.ce.model.domain.result.holding.TopCommonHoldingsResult;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.ce.model.dto.command.TopCommonHoldingsCommand;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.enumeration.LanguageCode;
import com.fintex.wm.commons.domain.holding.HoldingType;
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

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
class TopHoldingsE2ETest extends AbstractPortfolioCalculationE2ETest {

  @Override
  protected String metricPath() {
    return "top-common-holdings";
  }

  @Override
  protected String requestBodyForSmsUnavailableScenario() {
    return requestBodyForPositiveSmsScenario();
  }

  @Override
  protected String requestBodyForPositiveSmsScenario() {
    var command = new TopCommonHoldingsCommand();
    command.setMetric(CalculationMetric.TOP_COMMON_HOLDINGS);
    command.setNumOfTopCommonHoldings(5);
    command.setHoldings(List.of(
        holding(new SecurityIdentifier("AAA_PARENT", FiIdentifierType.TICKER),
            FinancialInstrumentType.ETF, Country.CANADA, 60_000),
        holding(new SecurityIdentifier("BBB_PARENT", FiIdentifierType.FUNDSERV),
            FinancialInstrumentType.ETF, Country.CANADA, 40_000)));
    command.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return writeJson(command);
  }

  @Override
  protected String smsPositiveResponseBody() {
    var response = List.of(
        securityAttributeResult(
            new SecurityIdentifier("AAA_PARENT", FiIdentifierType.TICKER),
            holdings(
                allocation("Alpha Corp", HoldingType.E, "20.0", "50000"),
                allocation("Bravo Corp", HoldingType.E, "18.0", "45000"),
                allocation("Charlie Corp", HoldingType.E, "16.0", "40000"),
                allocation("Delta Corp", HoldingType.E, "14.0", "35000"),
                allocation("Echo Corp", HoldingType.E, "12.0", "30000"),
                allocation("Foxtrot Corp", HoldingType.E, "10.0", "25000"))),
        securityAttributeResult(
            new SecurityIdentifier("BBB_PARENT", FiIdentifierType.FUNDSERV),
            holdings(
                allocation("Alpha Corp", HoldingType.E, "25.0", "60000"),
                allocation("Bravo Corp", HoldingType.E, "20.0", "48000"),
                allocation("Golf Corp", HoldingType.E, "15.0", "36000"),
                allocation("Hotel Corp", HoldingType.E, "12.0", "28800"),
                allocation("India Corp", HoldingType.E, "10.0", "24000"),
                allocation("Juliet Corp", HoldingType.E, "8.0", "19200"),
                allocation("Kilo Corp", HoldingType.E, "6.0", "14400"),
                allocation("Lima Corp", HoldingType.E, "4.0", "9600"))));

    return writeJson(response);
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    var command = new ReturnCommand();
    command.setMetric(CalculationMetric.GROWTH_OF_10K);
    command.setHoldings(List.of(holding(
        new SecurityIdentifier("AAA_PARENT", FiIdentifierType.TICKER),
        FinancialInstrumentType.ETF, Country.CANADA, 50_000)));
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
    // than preserving the SMS input order.
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
          HoldingType.E,
          weights[i - 1],
          "10000"));
    }

    // Shared Corp is the 26th holding in Parent B.
    parentBHoldings.add(allocation(
        "Shared Corp",
        HoldingType.E,
        "1.0",
        "5000"));

    var smsResponse = List.of(
        securityAttributeResult(
            parentAId,
            holdings(
                allocation(
                    "Shared Corp",
                    HoldingType.E,
                    "20.0",
                    "50000"))),
        securityAttributeResult(
            parentBId,
            holdings(
                parentBHoldings.toArray(SecurityHolding[]::new))));

    enqueueSmsMockResponse(writeJson(smsResponse));

    var command = new TopCommonHoldingsCommand();
    command.setMetric(CalculationMetric.TOP_COMMON_HOLDINGS);
    command.setNumOfTopCommonHoldings(10);
    command.setHoldings(List.of(
        holding(parentAId, FinancialInstrumentType.ETF, Country.CANADA, 60_000),
        holding(parentBId, FinancialInstrumentType.ETF, Country.CANADA, 40_000)));
    command.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    HttpResponse response = postCalculation(writeJson(command));

    assertThat(response.status().value())
        .isEqualTo(HttpStatus.OK.value());

    TopCommonHoldingsResult result = readJson(response.responseBody(), TopCommonHoldingsResult.class);

    assertThat(result.getWarnings()).isEmpty();

    // Verify the whole Top 10 result is ordered by calculated allocation
    // rather than by the SMS input order.
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

  private static Holdings holdings(SecurityHolding... allocations) {
    var h = new Holdings();
    h.setAllocation(List.of(allocations));
    h.setCurrency(Currency.CAD);
    h.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return h;
  }

  private static SecurityHolding allocation(
      String companyName,
      HoldingType type,
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
