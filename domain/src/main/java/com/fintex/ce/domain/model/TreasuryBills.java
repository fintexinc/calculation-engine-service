package com.fintex.ce.domain.model;

import com.fintex.sm.model.domain.enumeration.CurrencyType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class TreasuryBills extends BaseCalculationData<TreasuryBills> {

  private CurrencyType currency;
  private Map<LocalDate, BigDecimal> monthlyReturns;

}
