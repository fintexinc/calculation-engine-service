package com.fintex.ce.domain.dto;

import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;

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
  private FinancialInstrumentType holdingType;

  public AverageManagementExpenseCalculationDTO() {
  }

  public AverageManagementExpenseCalculationDTO(final BigDecimal marketValueQualified, final FinancialInstrumentType holdingType) {
    this.marketValueQualified = marketValueQualified;
    this.holdingType = holdingType;
  }

  public AverageManagementExpenseCalculationDTO(final Holding holding) {
    this.marketValueQualified = holding.getValue();
    this.holdingType = holding.getHoldingType();
  }
}
