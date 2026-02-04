package com.fintex.ce.adapter.rest.dto.request.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.util.FilterUtils;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fintex.ce.util.FilterUtils.filterHoldings;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public class PortfolioReqDTO {

  private List<Holding> holdings;

  private List<Holding> benchmarkHoldings;

  private Currency currency;

  public PortfolioReqDTO() {
  }

  public PortfolioReqDTO(final List<Holding> holdings, final Currency currency) {
    this.holdings = holdings;
    this.currency = currency;
  }

  public void setReqCurrencyToCashHolding() {
    final List<CashHolding> cashHoldings = filterHoldings(holdings, FilterUtils.CASH_PREDICATE)
        .stream().map(h -> (CashHolding) h)
        .collect(Collectors.toList());
    if (cashHoldings.size() == 1 && Objects.isNull(cashHoldings.get(0).getCurrency())) {
      cashHoldings.get(0).setCurrency(currency);
    }
  }

}
