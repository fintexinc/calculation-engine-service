package ca.tangerine.pce.model.domain.calculation.rate;

import ca.tangerine.pce.model.domain.calculation.BaseCalculationData;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class FxRates extends BaseCalculationData {

  private Map<LocalDate, FxRate> fxRates;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class FxRate {
    private BigDecimal usdCad;
    private BigDecimal cadUsd;
  }

}
