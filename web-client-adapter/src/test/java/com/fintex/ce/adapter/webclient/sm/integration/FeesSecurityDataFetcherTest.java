package com.fintex.ce.adapter.webclient.sm.integration;

import com.fintex.ce.adapter.webclient.sm.integration.fixture.FeesSmsResponseAppender;
import com.fintex.ce.adapter.webclient.sm.integration.fixture.FeesSmsResponseAppender.FeesValues;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.financial.Fees;
import com.fintex.wm.commons.domain.id.FiIdentifierType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;

import org.junit.jupiter.api.Tag;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FeesSecurityDataFetcherTest extends AbstractSecurityDataFetcherTest<FeeData, Fees> {

  private static final String etf1MorningstarId = "0P00001ABC";
  private static final String canadianFundFundservCode = "RBF605";

  @Autowired
  @Qualifier("feesFetcher")
  private SecurityDataFetcher<FeeData> feesFetcher;

  @Override
  protected SecurityDataFetcher<FeeData> fetcherUnderTest() {
    return feesFetcher;
  }

  @Override
  protected String endpointPath() {
    return "/fees";
  }

  @Override
  protected List<PortfolioHolding> holdingsForComplexScenario() {
    return List.of(
        createHolding(etf1MorningstarId, FiIdentifierType.MORNINGSTAR_ID, FinancialInstrumentType.ETF_CANADA),
        createHolding("ETF2", FiIdentifierType.TICKER, FinancialInstrumentType.ETF_CANADA),
        createHolding(
            canadianFundFundservCode, FiIdentifierType.FUNDSERV, FinancialInstrumentType.MUTUAL_FUND_CANADA));
  }

  @Override
  protected List<SecurityAttributeResult<Fees>> smsResponseForComplexScenario() {
    return new FeesSmsResponseAppender()
        .append(
            etf1MorningstarId,
            FiIdentifierType.MORNINGSTAR_ID,
            new FeesValues("0.0100", "0.0200", "0.0300", "0.0400", "0.0500"))
        .append(
            canadianFundFundservCode,
            FiIdentifierType.FUNDSERV,
            new FeesValues("0.0150", "0.0250", "0.0350", "0.0450", "0.0550"))
        .build();
  }

  @Override
  protected void assertComplexScenario(Map<PortfolioHolding, FeeData> result) {
    PortfolioHolding etf1 = holdingsForComplexScenario().get(0);
    PortfolioHolding fund1 = holdingsForComplexScenario().get(2);

    assertThat(result).containsOnlyKeys(etf1, fund1);

    FeeData etf1Fees = result.get(etf1);
    assertThat(etf1Fees.getHoldingId()).isEqualTo(etf1MorningstarId);
    assertThat(etf1Fees.getManagementFee()).isEqualByComparingTo("0.0100");
    assertThat(etf1Fees.getManagementExpenseRatio()).isEqualByComparingTo("0.0200");
    assertThat(etf1Fees.getNetExpenseRatio()).isEqualByComparingTo("0.0300");
    assertThat(etf1Fees.getGrossExpenseRatio()).isEqualByComparingTo("0.0400");
    assertThat(etf1Fees.getActual12B1Fee()).isEqualByComparingTo("0.0500");

    FeeData fund1Fees = result.get(fund1);
    assertThat(fund1Fees.getHoldingId()).isEqualTo(canadianFundFundservCode);
    assertThat(fund1Fees.getManagementFee()).isEqualByComparingTo("0.0150");
    assertThat(fund1Fees.getManagementExpenseRatio()).isEqualByComparingTo("0.0250");
    assertThat(fund1Fees.getNetExpenseRatio()).isEqualByComparingTo("0.0350");
    assertThat(fund1Fees.getGrossExpenseRatio()).isEqualByComparingTo("0.0450");
    assertThat(fund1Fees.getActual12B1Fee()).isEqualByComparingTo("0.0550");
  }

  @Override
  protected PortfolioHolding holdingForEmptyResponseScenario() {
    return createHolding("SEC-001", FiIdentifierType.TICKER, FinancialInstrumentType.ETF_CANADA);
  }

  @Override
  protected SecurityAttributeResult<Fees> responseForIdentifierNotPresentInRequest() {
    return new FeesSmsResponseAppender()
        .append(
            "NOT_REQUESTED",
            FiIdentifierType.TICKER,
            new FeesValues("0.0001", "0.0001", "0.0001", "0.0001", "0.0001"))
        .build()
        .getFirst();
  }
}
