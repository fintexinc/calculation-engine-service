package com.fintex.ce.port.input.result.distributionofreturns;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DistributionOfReturnsIntervalResult {

  private BigDecimal distributionMin;
  private BigDecimal distributionMax;
  private int distributionBin;
  private BigDecimal distributionIncrement;
  private List<DistributionRangeResult> distributionRange;
}
