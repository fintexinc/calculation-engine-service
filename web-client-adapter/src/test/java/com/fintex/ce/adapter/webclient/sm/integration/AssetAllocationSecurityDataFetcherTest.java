package com.fintex.ce.adapter.webclient.sm.integration;

import com.fintex.ce.adapter.webclient.sm.integration.fixture.AssetAllocationSmsResponseAppender;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.AssetAllocation;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.allocation.AssetAllocationValue;
import com.fintex.wm.commons.domain.allocation.AssetAllocationWithCurrency;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AssetAllocationSecurityDataFetcherTest
    extends
      AbstractSecurityDataFetcherTest<HoldingAssetAllocation, AssetAllocationWithCurrency> {

  private static final String etf1MorningstarId = "0P00001ABC";
  private static final String canadianFundFundservCode = "RBF605";
  private static final String usStockTickerMic = "AAPL";
  private static final String usStockExchangeMic = "XNAS";

  @Autowired
  @Qualifier("assetAllocationFetcher")
  private SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher;

  @Override
  protected SecurityDataFetcher<HoldingAssetAllocation> fetcherUnderTest() {
    return assetAllocationFetcher;
  }

  @Override
  protected String endpointPath() {
    return "/allocations/asset";
  }

  @Override
  protected List<PortfolioHolding> holdingsForComplexScenario() {
    return List.of(
        createHolding(etf1MorningstarId, FiIdentifierType.MORNINGSTAR_ID, FinancialInstrumentType.ETF_CANADA),
        createHolding("ETF2", FiIdentifierType.TICKER, FinancialInstrumentType.ETF_CANADA),
        createHolding(
            canadianFundFundservCode, FiIdentifierType.FUNDSERV, FinancialInstrumentType.MUTUAL_FUND_CANADA),
        createEquityHolding(
            usStockTickerMic, FiIdentifierType.TICKER_MIC, usStockExchangeMic, FinancialInstrumentType.STOCK_US));
  }

  @Override
  protected List<SecurityAttributeResult<AssetAllocationWithCurrency>> smsResponseForComplexScenario() {
    return new AssetAllocationSmsResponseAppender()
        .append(etf1MorningstarId, FiIdentifierType.MORNINGSTAR_ID,
            List.of(
                value(AssetAllocationRegionType.US_EQUITIES, "60.0"),
                value(AssetAllocationRegionType.FIXED_INCOME, "20.0"),
                value(AssetAllocationRegionType.CASH, "10.0"),
                value(AssetAllocationRegionType.OTHER, "5.0"),
                value(AssetAllocationRegionType.UNCLASSIFIED, "5.0")))
        .append(canadianFundFundservCode, FiIdentifierType.FUNDSERV,
            List.of(
                value(AssetAllocationRegionType.CANADIAN_EQUITIES, "70.0"),
                value(AssetAllocationRegionType.FIXED_INCOME, "15.0"),
                value(AssetAllocationRegionType.CASH, "5.0"),
                value(AssetAllocationRegionType.OTHER, "5.0"),
                value(AssetAllocationRegionType.UNCLASSIFIED, "5.0")))
        .append(usStockTickerMic, FiIdentifierType.TICKER_MIC,
            List.of(value(AssetAllocationRegionType.US_EQUITIES, "100.0")))
        .build();
  }

  @Override
  protected void assertComplexScenario(Map<PortfolioHolding, HoldingAssetAllocation> result) {
    PortfolioHolding etf1 = holdingsForComplexScenario().get(0);
    PortfolioHolding fund1 = holdingsForComplexScenario().get(2);
    PortfolioHolding stock1 = holdingsForComplexScenario().get(3);

    assertThat(result).containsOnlyKeys(etf1, fund1, stock1);

    assertThat(result.get(etf1).getAllocations()).containsExactlyInAnyOrderEntriesOf(Map.of(
        AssetAllocationRegionType.US_EQUITIES, new BigDecimal("60.0"),
        AssetAllocationRegionType.FIXED_INCOME, new BigDecimal("20.0"),
        AssetAllocationRegionType.CASH, new BigDecimal("10.0"),
        AssetAllocationRegionType.OTHER, new BigDecimal("5.0"),
        AssetAllocationRegionType.UNCLASSIFIED, new BigDecimal("5.0")));

    assertThat(result.get(fund1).getAllocations()).containsExactlyInAnyOrderEntriesOf(Map.of(
        AssetAllocationRegionType.CANADIAN_EQUITIES, new BigDecimal("70.0"),
        AssetAllocationRegionType.FIXED_INCOME, new BigDecimal("15.0"),
        AssetAllocationRegionType.CASH, new BigDecimal("5.0"),
        AssetAllocationRegionType.OTHER, new BigDecimal("5.0"),
        AssetAllocationRegionType.UNCLASSIFIED, new BigDecimal("5.0")));

    assertThat(result.get(stock1).getAllocations()).containsExactlyInAnyOrderEntriesOf(Map.of(
        AssetAllocationRegionType.US_EQUITIES, new BigDecimal("100.0")));
  }

  @Override
  protected PortfolioHolding holdingForEmptyResponseScenario() {
    return createHolding("SEC-001", FiIdentifierType.TICKER, FinancialInstrumentType.ETF_CANADA);
  }

  @Override
  protected SecurityAttributeResult<AssetAllocationWithCurrency> responseForIdentifierNotPresentInRequest() {
    SecurityIdentifier identifier = createSecurityIdentifier("NOT_REQUESTED", FiIdentifierType.TICKER);
    AssetAllocation allocation = new AssetAllocation();
    allocation.setAllocations(List.of(value(AssetAllocationRegionType.US_EQUITIES, "1.0")));
    AssetAllocationWithCurrency wrapper = new AssetAllocationWithCurrency();
    wrapper.setAssetAllocation(allocation);
    return securityAttributeResult(identifier, wrapper);
  }

  @Test
  void shouldMapProvidersAndCurrency_whenSmsReturnsThem() throws Exception {
    PortfolioHolding holding = createHolding(etf1MorningstarId, FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.ETF_CANADA);
    AssetAllocation allocation = new AssetAllocation();
    allocation.setAllocations(List.of(value(AssetAllocationRegionType.US_EQUITIES, "100.0")));
    allocation.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    CurrencyDatapoint currencyDp = new CurrencyDatapoint();
    currencyDp.setValue(Currency.USD);
    AssetAllocationWithCurrency wrapper = new AssetAllocationWithCurrency();
    wrapper.setAssetAllocation(allocation);
    wrapper.setCurrency(currencyDp);

    SecurityIdentifier identifier = createSecurityIdentifier(etf1MorningstarId, FiIdentifierType.MORNINGSTAR_ID);
    enqueueSmsJsonResponse(objectMapper.writeValueAsString(
        List.of(securityAttributeResult(identifier, wrapper))));

    Map<PortfolioHolding, HoldingAssetAllocation> result = fetcherUnderTest().fetch(List.of(holding),
        providersForComplexScenario());

    assertThat(takeSmsRequest().getPath()).isEqualTo("/api/v1/wealth/securities" + endpointPath());
    assertThat(result).containsOnlyKeys(holding);
    assertThat(result.get(holding).getProviders()).containsExactly(DataProvider.MORNINGSTAR);
    assertThat(result.get(holding).getCurrency()).isEqualTo(Currency.USD);
    assertThat(result.get(holding).getAllocations())
        .containsEntry(AssetAllocationRegionType.US_EQUITIES, new BigDecimal("100.0"));
  }

  private static AssetAllocationValue value(AssetAllocationRegionType type, String amount) {
    return new AssetAllocationValue(type, new BigDecimal(amount), new TreeSet<>());
  }
}
