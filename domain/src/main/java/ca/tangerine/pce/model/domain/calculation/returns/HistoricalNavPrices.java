package ca.tangerine.pce.model.domain.calculation.returns;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import ca.tangerine.pce.model.domain.calculation.BaseCalculationData;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class HistoricalNavPrices extends BaseCalculationData implements ReturnsData {

  private String currency;
  private FinancialInstrumentType holdingType;
  private TreeMap<LocalDate, BigDecimal> returns;
  private List<LocalDate> missedMonthData = new ArrayList<>();
  private List<LocalDate> missedDates = new ArrayList<>();

}
