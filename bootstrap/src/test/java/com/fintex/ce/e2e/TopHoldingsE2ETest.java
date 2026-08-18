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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
@Disabled("Temporarily disabled until Top Holdings document-aligned behavior is implemented. " +
    "JIRA: https://fintexinc.atlassian.net/browse/TMI-398")
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
        holding(new SecurityIdentifier("AAA_PARENT", FiIdentifierType.TICKER), 60_000),
        holding(new SecurityIdentifier("BBB_PARENT", FiIdentifierType.FUNDSERV), 40_000)));
    command.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return writeJson(command);
  }

  @Override
  protected String smsPositiveResponseBody() {
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

  private static PortfolioHolding holding(SecurityIdentifier securityIdentifier, int value) {
    return new PortfolioHolding(
        BigDecimal.valueOf(value),
        FinancialInstrumentType.ETF,
        Country.CANADA,
        securityIdentifier);
  }

  private static Holdings holdings(SecurityHolding... allocations) {
    var th = new Holdings();
    th.setAllocation(List.of(allocations));
    th.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    th.setCurrency(Currency.CAD);
    return th;
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
