package com.fintex.ce.model.domain.calculation.input;

import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.model.domain.enumeration.Rebalanced;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class WeightedAverageInput {

  protected LocalDate cipsd;

  private Rebalanced rebalanced;
  private DateRange dateRange;

  private Map<PortfolioHolding, Currency> holdings;

  private Map<PortfolioHolding, Map<LocalDate, BigDecimal>> portfolioReturns;
  private Map<CurrencyExchangePair, NavigableMap<LocalDate, BigDecimal>> fxRates;

  private Currency currency;

  public WeightedAverageInput() {
    this.rebalanced = Rebalanced.MONTHLY;
  }

  public WeightedAverageInput makeCopy() {
    return new WeightedAverageInput()
        .setCipsd(this.cipsd)
        .setRebalanced(this.rebalanced)
        .setDateRange(this.dateRange)
        .setHoldings(new HashMap<>(this.holdings))
        .setPortfolioReturns(new HashMap<>(this.portfolioReturns))
        .setFxRates(new HashMap<>(this.fxRates))
        .setCurrency(this.currency);
  }

}
