package ca.tangerine.pce.model.domain.calculation.input;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HoldingForDailyCalculation extends InvestmentData {

  private PortfolioHolding holding;

  public HoldingForDailyCalculation(final PortfolioHolding holding, final BigDecimal purchaseAmount) {
    this.holding = holding;
    this.setPurchaseAmount(purchaseAmount);
  }
}
