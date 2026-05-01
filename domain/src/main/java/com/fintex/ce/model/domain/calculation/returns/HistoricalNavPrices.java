package com.fintex.ce.model.domain.calculation.returns;

import com.fintex.ce.model.domain.calculation.BaseCalculationData;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class HistoricalNavPrices extends BaseCalculationData<HistoricalNavPrices> implements ReturnsData {

  private String currency;
  private FinancialInstrumentType holdingType;
  private TreeMap<LocalDate, BigDecimal> returns;
  private List<LocalDate> missedMonthData = new ArrayList<>();
  private List<LocalDate> missedDates = new ArrayList<>();

}
