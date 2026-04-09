package com.fintex.ce.domain.dto.calculation;

import com.fintex.ce.domain.model.CommonDates;
import com.fintex.ce.domain.model.FxRates;
import com.fintex.ce.domain.model.enumeration.Rebalanced;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.enumeration.CurrencyType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class WeightedAverageInputDTO {

  // custom interval portfolio start date
  protected LocalDate cipsd;

  private Rebalanced rebalanced;
  private CommonDates commonDates;

  private Map<Holding, CurrencyType> holdings;

  // already pre-formatted monthly returns
  private Map<Holding, Map<LocalDate, BigDecimal>> portfolioReturns;
  private Map<LocalDate, FxRates.FxRate> fxRates;

  private CurrencyType currency;

  public WeightedAverageInputDTO() {
    this.rebalanced = Rebalanced.MONTHLY;
  }

  public WeightedAverageInputDTO makeCopy() {
    return new WeightedAverageInputDTO()
        .setCipsd(this.cipsd)
        .setRebalanced(this.rebalanced)
        .setCommonDates(this.commonDates)
        .setHoldings(new HashMap<>(this.holdings))
        .setPortfolioReturns(new HashMap<>(this.portfolioReturns))
        .setFxRates(new HashMap<>(this.fxRates))
        .setCurrency(this.currency);
  }

}
