package com.fintex.ce.test;

import com.fintex.ce.model.domain.enumeration.InterestFreq;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.EquitySecurityIdentifier;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PortfolioHoldingBuildHelperTest {

  @Test
  void shouldBuildFundHolding_whenCanadianShortcutIsUsed() {
    PortfolioHolding holding = PortfolioHoldingBuildHelper.fundCa("FUND-1", 250L);

    assertThat(holding.getValue()).isEqualByComparingTo("250");
    assertThat(holding.getHoldingType()).isEqualTo(FinancialInstrumentType.MUTUAL_FUND);
    assertThat(holding.getCountry()).isEqualTo(Country.CANADA);
    assertThat(holding.getSecurityIdentifier().getId()).isEqualTo("FUND-1");
    assertThat(holding.getSecurityIdentifier().getIdType()).isEqualTo(FiIdentifierType.MORNINGSTAR_ID);
  }

  @Test
  void shouldBuildEquityHolding_whenExchangeParametersAreProvided() {
    PortfolioHolding holding = PortfolioHoldingBuildHelper.equity("SHOP", "XTSE", FinancialInstrumentType.STOCK,
        Country.CANADA, "125.50");

    assertThat(holding.getValue()).isEqualByComparingTo("125.50");
    assertThat(holding.getHoldingType()).isEqualTo(FinancialInstrumentType.STOCK);
    assertThat(holding.getCountry()).isEqualTo(Country.CANADA);
    assertThat(holding.getSecurityIdentifier()).isInstanceOf(EquitySecurityIdentifier.class);
    EquitySecurityIdentifier identifier = (EquitySecurityIdentifier) holding.getSecurityIdentifier();
    assertThat(identifier.getId()).isEqualTo("SHOP");
    assertThat(identifier.getIdType()).isEqualTo(FiIdentifierType.TICKER_MIC);
    assertThat(identifier.getExchangeId()).isEqualTo("XTSE");
  }

  @Test
  void shouldBuildCashHolding_whenCurrencyAndValueAreProvided() {
    CashHolding holding = PortfolioHoldingBuildHelper.cash(Currency.USD, new BigDecimal("75.25"));

    assertThat(holding.getValue()).isEqualByComparingTo("75.25");
    assertThat(holding.getHoldingType()).isEqualTo(FinancialInstrumentType.CASH);
    assertThat(holding.getCurrency()).isEqualTo(Currency.USD);
    assertThat(holding.getCountry()).isNull();
    assertThat(holding.getSecurityIdentifier()).isNull();
  }

  @Test
  void shouldBuildGicHolding_whenAllParametersAreProvided() {
    SecurityIdentifier identifier = new SecurityIdentifier("GIC-1", FiIdentifierType.CUSIP);
    LocalDate investmentDate = LocalDate.of(2025, 1, 15);

    GicHolding holding = PortfolioHoldingBuildHelper.gic(identifier, Currency.CAD, new BigDecimal("1000.00"),
        BigDecimal.valueOf(365), new BigDecimal("4.25"), InterestFreq.SEMI_ANNUAL, investmentDate);

    assertThat(holding.getValue()).isEqualByComparingTo("1000.00");
    assertThat(holding.getHoldingType()).isEqualTo(FinancialInstrumentType.GIC);
    assertThat(holding.getSecurityIdentifier()).isEqualTo(identifier);
    assertThat(holding.getCurrency()).isEqualTo(Currency.CAD);
    assertThat(holding.getTerm()).isEqualByComparingTo("365");
    assertThat(holding.getClientIntRate()).isEqualByComparingTo("4.25");
    assertThat(holding.getInterestFreq()).isEqualTo(InterestFreq.SEMI_ANNUAL);
    assertThat(holding.getInvestmentDate()).isEqualTo(investmentDate);
  }

  @Test
  void shouldBuildHoldingWithoutCountry_whenCountryIsIntentionallyAbsent() {
    PortfolioHolding holding = PortfolioHoldingBuildHelper.holdingWithoutCountry("ETF-1", FiIdentifierType.ISIN,
        FinancialInstrumentType.ETF, BigDecimal.TEN);

    assertThat(holding.getValue()).isEqualByComparingTo("10");
    assertThat(holding.getHoldingType()).isEqualTo(FinancialInstrumentType.ETF);
    assertThat(holding.getCountry()).isNull();
    assertThat(holding.getSecurityIdentifier().getId()).isEqualTo("ETF-1");
    assertThat(holding.getSecurityIdentifier().getIdType()).isEqualTo(FiIdentifierType.ISIN);
  }

  @Test
  void shouldBuildMinimalHolding_whenOptionalIdentifierTypeAndValueAreAbsent() {
    PortfolioHolding holding = PortfolioHoldingBuildHelper.holding("ETF-2", FinancialInstrumentType.ETF,
        Country.CANADA);

    assertThat(holding.getValue()).isNull();
    assertThat(holding.getHoldingType()).isEqualTo(FinancialInstrumentType.ETF);
    assertThat(holding.getCountry()).isEqualTo(Country.CANADA);
    assertThat(holding.getSecurityIdentifier().getId()).isEqualTo("ETF-2");
    assertThat(holding.getSecurityIdentifier().getIdType()).isNull();
  }

  @Test
  void shouldBuildMorningstarEtf_whenMorningstarShortcutIsUsed() {
    PortfolioHolding holding = PortfolioHoldingBuildHelper.etfCaByMorningstarId("ETF-3");

    assertThat(holding.getValue()).isNull();
    assertThat(holding.getHoldingType()).isEqualTo(FinancialInstrumentType.ETF);
    assertThat(holding.getCountry()).isEqualTo(Country.CANADA);
    assertThat(holding.getSecurityIdentifier().getId()).isEqualTo("ETF-3");
    assertThat(holding.getSecurityIdentifier().getIdType()).isEqualTo(FiIdentifierType.MORNINGSTAR_ID);
  }

  @Test
  void shouldPreserveCashIdentifier_whenIdentifierIsProvided() {
    CashHolding holding = PortfolioHoldingBuildHelper.cash("CASH-CAD", FiIdentifierType.TICKER, Currency.CAD, 50L);

    assertThat(holding.getValue()).isEqualByComparingTo("50");
    assertThat(holding.getHoldingType()).isEqualTo(FinancialInstrumentType.CASH);
    assertThat(holding.getCurrency()).isEqualTo(Currency.CAD);
    assertThat(holding.getSecurityIdentifier().getId()).isEqualTo("CASH-CAD");
    assertThat(holding.getSecurityIdentifier().getIdType()).isEqualTo(FiIdentifierType.TICKER);
  }

  @Test
  void shouldBuildExchangeHoldings_whenDefaultNasdaqShortcutsAreUsed() {
    PortfolioHolding stock = PortfolioHoldingBuildHelper.stock("MSFT");
    PortfolioHolding etf = PortfolioHoldingBuildHelper.etf("QQQ");

    assertThat(stock.getValue()).isEqualByComparingTo("1");
    assertThat(stock.getHoldingType()).isEqualTo(FinancialInstrumentType.STOCK);
    assertThat(stock.getCountry()).isEqualTo(Country.USA);
    assertThat(stock.getSecurityIdentifier()).isInstanceOf(EquitySecurityIdentifier.class);
    EquitySecurityIdentifier stockIdentifier = (EquitySecurityIdentifier) stock.getSecurityIdentifier();
    assertThat(stockIdentifier.getId()).isEqualTo("MSFT");
    assertThat(stockIdentifier.getIdType()).isEqualTo(FiIdentifierType.TICKER);
    assertThat(stockIdentifier.getExchangeId()).isEqualTo("XNAS");

    assertThat(etf.getValue()).isEqualByComparingTo("1");
    assertThat(etf.getHoldingType()).isEqualTo(FinancialInstrumentType.ETF);
    assertThat(etf.getCountry()).isEqualTo(Country.USA);
    assertThat(etf.getSecurityIdentifier()).isInstanceOf(EquitySecurityIdentifier.class);
    EquitySecurityIdentifier etfIdentifier = (EquitySecurityIdentifier) etf.getSecurityIdentifier();
    assertThat(etfIdentifier.getId()).isEqualTo("QQQ");
    assertThat(etfIdentifier.getIdType()).isEqualTo(FiIdentifierType.TICKER);
    assertThat(etfIdentifier.getExchangeId()).isEqualTo("XNAS");
  }

  @Test
  void shouldBuildFundservHolding_whenMutualFundShortcutIsUsed() {
    PortfolioHolding holding = PortfolioHoldingBuildHelper.mutualFund("RBF123");

    assertThat(holding.getValue()).isEqualByComparingTo("1");
    assertThat(holding.getHoldingType()).isEqualTo(FinancialInstrumentType.MUTUAL_FUND);
    assertThat(holding.getCountry()).isEqualTo(Country.CANADA);
    assertThat(holding.getSecurityIdentifier().getId()).isEqualTo("RBF123");
    assertThat(holding.getSecurityIdentifier().getIdType()).isEqualTo(FiIdentifierType.FUNDSERV);
  }

  @Test
  void shouldBuildCanadianEtf_whenTickerShortcutIsUsed() {
    PortfolioHolding holding = PortfolioHoldingBuildHelper.etfCa("XIU");

    assertThat(holding.getValue()).isEqualByComparingTo("1");
    assertThat(holding.getHoldingType()).isEqualTo(FinancialInstrumentType.ETF);
    assertThat(holding.getCountry()).isEqualTo(Country.CANADA);
    assertThat(holding.getSecurityIdentifier().getId()).isEqualTo("XIU");
    assertThat(holding.getSecurityIdentifier().getIdType()).isEqualTo(FiIdentifierType.TICKER);
  }
}
