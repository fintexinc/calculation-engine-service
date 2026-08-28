package ca.tangerine.pce.model.domain.result.correlation;

import ca.tangerine.pce.model.domain.holding.CashHolding;
import ca.tangerine.pce.model.domain.holding.GicHolding;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.EquitySecurityIdentifier;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HoldingsKeyResult(
    FinancialInstrumentType type,
    SecurityIdentifier securityIdentifier,
    String key,
    BigDecimal allocation,
    String name,
    Currency currency) {

  public static HoldingsKeyResult buildHoldingsKeyResult(final PortfolioHolding holding) {
    return buildFromHolding(holding, null);
  }

  public static HoldingsKeyResult buildFromHolding(final PortfolioHolding holding, final BigDecimal allocation) {
    final FinancialInstrumentType type = holding.getHoldingType();
    final Currency currency = FinancialInstrumentType.CASH.equals(type) ? ((CashHolding) holding).getCurrency() : null;
    final String name = FinancialInstrumentType.GIC.equals(type) ? ((GicHolding) holding).getName() : null;
    return new HoldingsKeyResult(type, holding.getSecurityIdentifier(), createKey(holding), allocation, name, currency);
  }

  private static String createKey(final PortfolioHolding holding) {
    String result;
    SecurityIdentifier secId = holding.getSecurityIdentifier();

    if (FinancialInstrumentType.CASH.equals(holding.getHoldingType())) {
      CashHolding cashHolding = (CashHolding) holding;
      result = cashHolding.getCurrency() != null ? cashHolding.getCurrency().name() : "";
    } else if (secId instanceof EquitySecurityIdentifier eqId) {
      result = secId.getId() + "_" + eqId.getExchangeId();
    } else {
      result = secId != null ? secId.getId() : "";
    }
    return holding.getHoldingType() + "_" + result;
  }
}