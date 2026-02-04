package com.fintex.ce.domain.model;

import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.Holding;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class AverageManagementExpenseCalculationDTO {

  private BigDecimal managementExpenseRatio;
  private BigDecimal netExpenseRatio;
  private BigDecimal grossExpenseRatio;
  private BigDecimal actualManagementFee;
  private BigDecimal initialFee;
  private BigDecimal modifiedFee;
  private BigDecimal marketValue;
  private BigDecimal percentage;
  private BigDecimal marketValueQualified;
  private BigDecimal percentageQualified;
  private HoldingType holdingType;

  public AverageManagementExpenseCalculationDTO() {
  }

  public AverageManagementExpenseCalculationDTO(final BigDecimal marketValueQualified, final HoldingType holdingType) {
    this.marketValueQualified = marketValueQualified;
    this.holdingType = holdingType;
  }

  public AverageManagementExpenseCalculationDTO(final Holding holding) {
    this.marketValueQualified = holding.getValue();
    this.holdingType = holding.getType();
  }
}
