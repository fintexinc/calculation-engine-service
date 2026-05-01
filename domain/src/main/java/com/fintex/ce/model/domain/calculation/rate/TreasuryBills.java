package com.fintex.ce.model.domain.calculation.rate;

import com.fintex.ce.model.domain.calculation.BaseCalculationData;
import com.fintex.wm.commons.domain.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class TreasuryBills extends BaseCalculationData {

  private Currency currency;
  private Map<LocalDate, BigDecimal> monthlyReturns;

}
