package ca.tangerine.pce.model.domain.calculation.fee;

import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;

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
  private BigDecimal modifiedFee;
  private BigDecimal marketValue;
  private FinancialInstrumentType holdingType;
  private Currency currency;
}
