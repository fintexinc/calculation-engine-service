package com.fintex.ce.domain.dto.command;

import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.Holding;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
public abstract class PortfolioCommand {
  private List<Holding> holdings;
  private List<Holding> benchmarkHoldings;
  private Currency currency;

  public void setReqCurrencyToCashHolding() {
    final List<CashHolding> cashHoldings = holdings.stream()
        .filter(h -> h.getType() == HoldingType.CASH)
        .map(h -> (CashHolding) h)
        .collect(Collectors.toList());
    if (cashHoldings.size() == 1 && Objects.isNull(cashHoldings.get(0).getCurrency())) {
      cashHoldings.get(0).setCurrency(currency);
    }
  }
}
