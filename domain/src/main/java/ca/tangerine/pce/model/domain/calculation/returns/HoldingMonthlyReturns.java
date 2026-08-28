package ca.tangerine.pce.model.domain.calculation.returns;

import ca.tangerine.pce.model.domain.calculation.BaseCalculationData;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.TreeMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HoldingMonthlyReturns extends BaseCalculationData implements ReturnsData {

  private String currency;
  private FinancialInstrumentType holdingType;
  private TreeMap<LocalDate, BigDecimal> returns;

}
