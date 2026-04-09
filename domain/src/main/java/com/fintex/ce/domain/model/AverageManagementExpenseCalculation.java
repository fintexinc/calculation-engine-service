package com.fintex.ce.domain.model;

import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AverageManagementExpenseCalculation {

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
}
