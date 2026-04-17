package com.fintex.ce.adapter.webclient.sm.integration;

import com.fintex.ce.adapter.webclient.sm.integration.fixture.AssetAllocationSmsResponseAppender;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.allocation.AssetAllocation;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.value.NameValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;

import org.junit.jupiter.api.Tag;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AssetAllocationSecurityDataFetcherTest
    extends
      AbstractSecurityDataFetcherTest<HoldingAssetAllocation, AssetAllocation> {

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
  protected List<SecurityAttributeResult<AssetAllocation>> smsResponseForComplexScenario() {
    return new AssetAllocationSmsResponseAppender()
        .append(
            etf1MorningstarId,
            FiIdentifierType.MORNINGSTAR_ID,
            List.of(
                new NameValue("EQUITY", new BigDecimal("60.0")),
                new NameValue("FIXED_INCOME", new BigDecimal("20.0")),
                new NameValue("CASH", new BigDecimal("10.0")),
                new NameValue("OTHER", new BigDecimal("5.0")),
                new NameValue("UNKNOWN", new BigDecimal("5.0"))))
        .append(
            canadianFundFundservCode,
            FiIdentifierType.FUNDSERV,
            List.of(
                new NameValue("EQUITY", new BigDecimal("70.0")),
                new NameValue("FIXED_INCOME", new BigDecimal("15.0")),
                new NameValue("CASH", new BigDecimal("5.0")),
                new NameValue("OTHER", new BigDecimal("5.0")),
                new NameValue("UNKNOWN", new BigDecimal("5.0"))))
        .append(
            usStockTickerMic,
            FiIdentifierType.TICKER_MIC,
            List.of(
                new NameValue("EQUITY", new BigDecimal("100.0")),
                new NameValue("FIXED_INCOME", new BigDecimal("0.0")),
                new NameValue("CASH", new BigDecimal("0.0")),
                new NameValue("OTHER", new BigDecimal("0.0")),
                new NameValue("UNKNOWN", new BigDecimal("0.0"))))
        .build();
  }

  @Override
  protected void assertComplexScenario(Map<PortfolioHolding, HoldingAssetAllocation> result) {
    PortfolioHolding etf1 = holdingsForComplexScenario().get(0);
    PortfolioHolding fund1 = holdingsForComplexScenario().get(2);
    PortfolioHolding stock1 = holdingsForComplexScenario().get(3);

    assertThat(result).containsOnlyKeys(etf1, fund1, stock1);

    assertThat(result.get(etf1).getAllocations())
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "EQUITY", new BigDecimal("60.0"),
                "FIXED_INCOME", new BigDecimal("20.0"),
                "CASH", new BigDecimal("10.0"),
                "OTHER", new BigDecimal("5.0"),
                "UNKNOWN", new BigDecimal("5.0")));

    assertThat(result.get(fund1).getAllocations())
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "EQUITY", new BigDecimal("70.0"),
                "FIXED_INCOME", new BigDecimal("15.0"),
                "CASH", new BigDecimal("5.0"),
                "OTHER", new BigDecimal("5.0"),
                "UNKNOWN", new BigDecimal("5.0")));

    assertThat(result.get(stock1).getAllocations())
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "EQUITY", new BigDecimal("100.0"),
                "FIXED_INCOME", new BigDecimal("0.0"),
                "CASH", new BigDecimal("0.0"),
                "OTHER", new BigDecimal("0.0"),
                "UNKNOWN", new BigDecimal("0.0")));
  }

  @Override
  protected PortfolioHolding holdingForEmptyResponseScenario() {
    return createHolding("SEC-001", FiIdentifierType.TICKER, FinancialInstrumentType.ETF_CANADA);
  }

  @Override
  protected SecurityAttributeResult<AssetAllocation> responseForIdentifierNotPresentInRequest() {
    SecurityIdentifier identifier = createSecurityIdentifier("NOT_REQUESTED", FiIdentifierType.TICKER);
    AssetAllocation allocation = new AssetAllocation();
    allocation.setAllocation(List.of(new NameValue("EQUITY", new BigDecimal("1.0"))));
    return securityAttributeResult(identifier, allocation);
  }
}
