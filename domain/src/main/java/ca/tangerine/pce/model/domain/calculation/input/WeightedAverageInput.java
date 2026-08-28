package ca.tangerine.pce.model.domain.calculation.input;

import ca.tangerine.pce.model.domain.CurrencyExchangePair;
import ca.tangerine.pce.model.domain.calculation.DateRange;
import ca.tangerine.pce.model.domain.enumeration.Rebalanced;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.wm.commons.domain.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeightedAverageInput {

  protected LocalDate cipsd;

  @Builder.Default
  private Rebalanced rebalanced = Rebalanced.MONTHLY;

  private DateRange dateRange;

  private Map<PortfolioHolding, Currency> holdings;

  private Map<PortfolioHolding, Map<LocalDate, BigDecimal>> portfolioReturns;
  private Map<CurrencyExchangePair, NavigableMap<LocalDate, BigDecimal>> fxRates;

  private Currency currency;

  public WeightedAverageInput makeCopy() {
    return WeightedAverageInput.builder()
        .cipsd(this.cipsd)
        .rebalanced(this.rebalanced)
        .dateRange(this.dateRange)
        .holdings(new HashMap<>(this.holdings))
        .portfolioReturns(new HashMap<>(this.portfolioReturns))
        .fxRates(new HashMap<>(this.fxRates))
        .currency(this.currency)
        .build();
  }

}