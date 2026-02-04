package com.fintex.ce.port.input.command;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.util.FilterUtils;
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
    final List<CashHolding> cashHoldings = FilterUtils.filterHoldings(holdings, FilterUtils.CASH_PREDICATE)
        .stream().map(h -> (CashHolding) h)
        .collect(Collectors.toList());
    if (cashHoldings.size() == 1 && Objects.isNull(cashHoldings.get(0).getCurrency())) {
      cashHoldings.get(0).setCurrency(currency);
    }
  }
}
