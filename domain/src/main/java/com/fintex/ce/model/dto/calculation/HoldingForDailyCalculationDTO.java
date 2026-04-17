package com.fintex.ce.model.dto.calculation;

import com.fintex.ce.model.domain.holding.PortfolioHolding;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HoldingForDailyCalculationDTO extends InvestmentDataDTO {

  private PortfolioHolding holding;

  public HoldingForDailyCalculationDTO(final PortfolioHolding holding, final BigDecimal purchaseAmount) {
    this.holding = holding;
    this.setPurchaseAmount(purchaseAmount);
  }
}
