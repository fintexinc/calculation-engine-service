package com.fintex.ce.e2e;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PortfolioHoldingBuildHelper {

  public static PortfolioHolding fundCa(String morningstarId, long value) {
    return fundOfCountry(morningstarId, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, value);
  }

  public static PortfolioHolding fundOfCountry(String morningstarId, FinancialInstrumentType type, Country country,
      long value) {
    return holdingOfCountry(new SecurityIdentifier(morningstarId, FiIdentifierType.MORNINGSTAR_ID), type, country,
        BigDecimal.valueOf(value));
  }

  public static PortfolioHolding etfCa(String ticker, long value) {
    return etfOfCountry(ticker, FinancialInstrumentType.ETF, Country.CANADA, value);
  }

  public static PortfolioHolding etfOfCountry(String ticker, FinancialInstrumentType type, Country country,
      long value) {
    return holdingOfCountry(new SecurityIdentifier(ticker, FiIdentifierType.TICKER), type, country,
        BigDecimal.valueOf(value));
  }

  public static PortfolioHolding stockCa(String ticker, String exchange, long value) {
    return equityOfCountry(ticker, exchange, FinancialInstrumentType.STOCK, Country.CANADA, value);
  }

  public static PortfolioHolding equityOfCountry(String ticker, String exchange, FinancialInstrumentType type,
      Country country, long value) {
    return equityOfCountry(ticker, exchange, type, country, BigDecimal.valueOf(value));
  }

  public static PortfolioHolding equityOfCountry(String ticker, String exchange, FinancialInstrumentType type,
      Country country, String value) {
    return equityOfCountry(ticker, exchange, type, country, new BigDecimal(value));
  }

  public static PortfolioHolding equityOfCountry(String ticker, String exchange, FinancialInstrumentType type,
      Country country, BigDecimal value) {
    return holdingOfCountry(
        EquitySecurityIdentifier.builder()
            .id(ticker)
            .idType(FiIdentifierType.TICKER_MIC)
            .exchangeId(exchange)
            .build(),
        type,
        country,
        value);
  }

  public static PortfolioHolding holdingOfCountry(SecurityIdentifier securityIdentifier, FinancialInstrumentType type,
      Country country, BigDecimal value) {
    return new PortfolioHolding(value, type, country, securityIdentifier);
  }

  public static PortfolioHolding holdingOfCountry(SecurityIdentifier securityIdentifier, FinancialInstrumentType type,
      Country country, String value) {
    return holdingOfCountry(securityIdentifier, type, country, new BigDecimal(value));
  }

  public static CashHolding cash(Currency currency, long value) {
    return CashHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.CASH)
        .currency(currency)
        .build();
  }

  public static GicHolding gic(String id, Currency currency, long value) {
    return GicHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.GIC)
        .securityIdentifier(new SecurityIdentifier(id, FiIdentifierType.TICKER))
        .currency(currency)
        .investmentDate(LocalDate.of(2024, 9, 1))
        .clientIntRate(new BigDecimal("4.75"))
        .interestFreq(InterestFreq.ANNUAL)
        .term(BigDecimal.valueOf(730))
        .build();
  }

  public static GicHolding gic(String id, Currency currency, long value, long termDays) {
    return GicHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.GIC)
        .securityIdentifier(new SecurityIdentifier(id, FiIdentifierType.TICKER))
        .currency(currency)
        .term(BigDecimal.valueOf(termDays))
        .build();
  }
}
