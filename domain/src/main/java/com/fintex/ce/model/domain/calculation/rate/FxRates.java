package com.fintex.ce.model.domain.calculation.rate;

import com.fintex.ce.model.domain.calculation.BaseCalculationData;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import lombok.AllArgsConstructor;
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
public class FxRates extends BaseCalculationData<FxRates> {

  private Map<LocalDate, FxRate> fxRates;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class FxRate {
    private BigDecimal usdCad;
    private BigDecimal cadUsd;
  }

}
