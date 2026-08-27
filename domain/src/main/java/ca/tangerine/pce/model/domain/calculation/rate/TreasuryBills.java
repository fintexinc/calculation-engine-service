package ca.tangerine.pce.model.domain.calculation.rate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import ca.tangerine.pce.model.domain.calculation.BaseCalculationData;
import ca.tangerine.wm.commons.domain.currency.Currency;
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class TreasuryBills extends BaseCalculationData {

  private Currency currency;
  private Map<LocalDate, BigDecimal> monthlyReturns;

}
