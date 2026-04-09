package com.fintex.ce.domain.dto.calculation;

import com.fintex.ce.domain.model.holding.Holding;

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

  private Holding holding;

  public HoldingForDailyCalculationDTO(final Holding holding, final BigDecimal purchaseAmount) {
    this.holding = holding;
    this.setPurchaseAmount(purchaseAmount);
  }
}
