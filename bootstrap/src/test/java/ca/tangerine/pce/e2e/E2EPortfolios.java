package ca.tangerine.pce.e2e;

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
import lombok.experimental.UtilityClass;

/**
 * Holding builders shared by the bootstrap-level e2e tests. Which identifier type a holding is addressed by is a
 * property of the instrument rather than of the test — funds by Morningstar id, ETFs by ticker, individual companies by
 * ticker-and-exchange — so each builder fixes that pairing once and a fixture cannot quietly address a fund the way an
 * ETF is addressed.
 *
 * <p>
 * Country defaults to Canada, which is what the portfolios under test hold; the overloads taking one are for the
 * scenarios where the holding's country is itself the subject (US fee resolution, US-listed securities).
 */
@UtilityClass
final class E2EPortfolios {

  static PortfolioHolding fund(String morningstarId, long value) {
    return fund(morningstarId, FinancialInstrumentType.MUTUAL_FUND, value);
  }

  static PortfolioHolding fund(String morningstarId, FinancialInstrumentType type, long value) {
    return fund(morningstarId, type, Country.CANADA, value);
  }

  static PortfolioHolding fund(String morningstarId, FinancialInstrumentType type, Country country, long value) {
    return new PortfolioHolding(BigDecimal.valueOf(value), type, country,
        new SecurityIdentifier(morningstarId, FiIdentifierType.MORNINGSTAR_ID));
  }

  static PortfolioHolding etf(String ticker, long value) {
    return etf(ticker, Country.CANADA, value);
  }

  static PortfolioHolding etf(String ticker, Country country, long value) {
    return new PortfolioHolding(BigDecimal.valueOf(value), FinancialInstrumentType.ETF, country,
        new SecurityIdentifier(ticker, FiIdentifierType.TICKER));
  }

  /**
   * An individual bond, which the sleeve metrics treat as the mirror image of a stock: it carries fixed income and no
   * equity, so it belongs in the fixed-income breakdowns' denominator and outside the equity ones'.
   */
  static PortfolioHolding bond(String ticker, long value) {
    return new PortfolioHolding(BigDecimal.valueOf(value), FinancialInstrumentType.FIXED_INCOME, Country.CANADA,
        new SecurityIdentifier(ticker, FiIdentifierType.TICKER));
  }

  static PortfolioHolding stock(String ticker, String exchange, long value) {
    return stock(ticker, exchange, Country.CANADA, value);
  }

  static PortfolioHolding stock(String ticker, String exchange, Country country, long value) {
    return new PortfolioHolding(BigDecimal.valueOf(value), FinancialInstrumentType.STOCK, country,
        EquitySecurityIdentifier.builder().id(ticker).idType(FiIdentifierType.TICKER_MIC).exchangeId(exchange).build());
  }

  static CashHolding cash(String id, Currency currency, long value) {
    return CashHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.CASH)
        .securityIdentifier(new SecurityIdentifier(id, FiIdentifierType.TICKER))
        .currency(currency)
        .build();
  }

  /**
   * {@code termDays} is not decoration: the breakdown metrics bucket a GIC by its term — under a year it is a
   * short-term investment, beyond that it sits with the bonds — so a GIC fixture without one gets bucketed by accident.
   */
  static GicHolding gic(String id, Currency currency, long value, long termDays) {
    return GicHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.GIC)
        .securityIdentifier(new SecurityIdentifier(id, FiIdentifierType.TICKER))
        .currency(currency)
        .term(BigDecimal.valueOf(termDays))
        .build();
  }
}
