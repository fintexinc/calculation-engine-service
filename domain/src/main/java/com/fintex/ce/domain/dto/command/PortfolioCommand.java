package com.fintex.ce.domain.dto.command;

import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.Holding;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public abstract class PortfolioCommand implements CalculationCommand {
  private List<Holding> holdings;
  private List<Holding> benchmarkHoldings;
  private Currency currency;

  public void setReqCurrencyToCashHolding() {
    final List<CashHolding> cashHoldings = holdings.stream()
        .filter(h -> h.getHoldingType() == FinancialInstrumentType.CASH)
        .map(h -> (CashHolding) h)
        .collect(Collectors.toList());
    if (cashHoldings.size() == 1 && Objects.isNull(cashHoldings.get(0).getCurrency())) {
      cashHoldings.get(0).setCurrency(currency);
    }
  }
}
