package ca.tangerine.pce.util;

import ca.tangerine.pce.model.domain.enumeration.InterestFreq;
import ca.tangerine.pce.model.domain.holding.CashHolding;
import ca.tangerine.pce.model.domain.holding.GicHolding;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.EquitySecurityIdentifier;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PortfolioHoldingBuildHelper {

  private static final String NASDAQ_EXCHANGE = "XNAS";

  public static PortfolioHolding fundCa(String morningstarId, long value) {
    return fund(morningstarId, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, value);
  }

  public static PortfolioHolding fund(String morningstarId, FinancialInstrumentType type, Country country,
      long value) {
    return holding(new SecurityIdentifier(morningstarId, FiIdentifierType.MORNINGSTAR_ID), type, country,
        BigDecimal.valueOf(value));
  }

  public static PortfolioHolding etfCa(String ticker, long value) {
    return etf(ticker, Country.CANADA, value);
  }

  public static PortfolioHolding etf(String ticker, Country country, long value) {
    return holding(new SecurityIdentifier(ticker, FiIdentifierType.TICKER), FinancialInstrumentType.ETF, country,
        BigDecimal.valueOf(value));
  }

  public static PortfolioHolding stockCa(String ticker, String exchange, long value) {
    return equity(ticker, exchange, FinancialInstrumentType.STOCK, Country.CANADA, value);
  }

  public static PortfolioHolding stock(String ticker) {
    return stock(ticker, BigDecimal.ONE);
  }

  public static PortfolioHolding stock(String ticker, BigDecimal value) {
    return stock(ticker, NASDAQ_EXCHANGE, Country.USA, value);
  }

  public static PortfolioHolding stock(String ticker, String exchange, Country country, BigDecimal value) {
    return exchangeHolding(ticker, exchange, FinancialInstrumentType.STOCK, country, value);
  }

  public static PortfolioHolding etf(String ticker) {
    return etf(ticker, BigDecimal.ONE);
  }

  public static PortfolioHolding etf(String ticker, BigDecimal value) {
    return etf(ticker, NASDAQ_EXCHANGE, Country.USA, value);
  }

  public static PortfolioHolding etf(String ticker, String exchange, Country country, BigDecimal value) {
    return exchangeHolding(ticker, exchange, FinancialInstrumentType.ETF, country, value);
  }

  public static PortfolioHolding mutualFund(String fundserv) {
    return holding(fundserv, FiIdentifierType.FUNDSERV, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA,
        BigDecimal.ONE);
  }

  public static PortfolioHolding equity(String ticker, String exchange, FinancialInstrumentType type, Country country,
      long value) {
    return equity(ticker, exchange, type, country, BigDecimal.valueOf(value));
  }

  public static PortfolioHolding equity(String ticker, String exchange, FinancialInstrumentType type, Country country,
      String value) {
    return equity(ticker, exchange, type, country, new BigDecimal(value));
  }

  public static PortfolioHolding equity(String ticker, String exchange, FinancialInstrumentType type, Country country,
      BigDecimal value) {
    return holding(
        EquitySecurityIdentifier.builder()
            .id(ticker)
            .idType(FiIdentifierType.TICKER_MIC)
            .exchangeId(exchange)
            .build(),
        type,
        country,
        value);
  }

  private static PortfolioHolding exchangeHolding(String ticker, String exchange, FinancialInstrumentType type,
      Country country, BigDecimal value) {
    return holding(
        EquitySecurityIdentifier.builder()
            .id(ticker)
            .idType(FiIdentifierType.TICKER)
            .exchangeId(exchange)
            .build(),
        type,
        country,
        value);
  }

  public static PortfolioHolding holdingByMorningstarId(String id, FinancialInstrumentType type, Country country,
      String value) {
    return holding(id, FiIdentifierType.MORNINGSTAR_ID, type, country, value);
  }

  public static PortfolioHolding holding(String id, FiIdentifierType identifierType, FinancialInstrumentType type,
      Country country, long value) {
    return holding(id, identifierType, type, country, BigDecimal.valueOf(value));
  }

  public static PortfolioHolding holding(String id, FiIdentifierType identifierType, FinancialInstrumentType type,
      Country country, String value) {
    return holding(id, identifierType, type, country, new BigDecimal(value));
  }

  public static PortfolioHolding holding(String id, FiIdentifierType identifierType, FinancialInstrumentType type,
      Country country, BigDecimal value) {
    return holding(new SecurityIdentifier(id, identifierType), type, country, value);
  }

  public static PortfolioHolding holding(SecurityIdentifier securityIdentifier, FinancialInstrumentType type,
      Country country, long value) {
    return holding(securityIdentifier, type, country, BigDecimal.valueOf(value));
  }

  public static PortfolioHolding holding(SecurityIdentifier securityIdentifier, FinancialInstrumentType type,
      Country country, String value) {
    return holding(securityIdentifier, type, country, new BigDecimal(value));
  }

  public static PortfolioHolding holding(SecurityIdentifier securityIdentifier, FinancialInstrumentType type,
      Country country, BigDecimal value) {
    return new PortfolioHolding(value, type, country, securityIdentifier);
  }

  public static PortfolioHolding holdingWithoutCountry(String id, FiIdentifierType identifierType,
      FinancialInstrumentType type, BigDecimal value) {
    return holdingWithoutCountry(new SecurityIdentifier(id, identifierType), type, value);
  }

  public static PortfolioHolding holdingWithoutCountry(SecurityIdentifier securityIdentifier,
      FinancialInstrumentType type, BigDecimal value) {
    return new PortfolioHolding(value, type, securityIdentifier);
  }

  public static CashHolding cash(Currency currency, long value) {
    return cash(currency, BigDecimal.valueOf(value));
  }

  public static CashHolding cash(Currency currency, String value) {
    return cash(currency, new BigDecimal(value));
  }

  public static CashHolding cash(Currency currency, BigDecimal value) {
    return CashHolding.builder()
        .value(value)
        .holdingType(FinancialInstrumentType.CASH)
        .currency(currency)
        .build();
  }

  public static GicHolding gic(SecurityIdentifier securityIdentifier, Currency currency, BigDecimal value,
      BigDecimal termDays) {
    return gic(securityIdentifier, currency, value, termDays, null, null, null);
  }

  public static GicHolding gic(SecurityIdentifier securityIdentifier, Currency currency, BigDecimal value,
      BigDecimal termDays, BigDecimal clientIntRate, InterestFreq interestFreq, LocalDate investmentDate) {
    return GicHolding.builder()
        .value(value)
        .holdingType(FinancialInstrumentType.GIC)
        .securityIdentifier(securityIdentifier)
        .currency(currency)
        .term(termDays)
        .clientIntRate(clientIntRate)
        .interestFreq(interestFreq)
        .investmentDate(investmentDate)
        .build();
  }
}
