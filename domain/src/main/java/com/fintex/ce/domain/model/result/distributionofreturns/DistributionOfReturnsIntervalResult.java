package com.fintex.ce.domain.model.result.distributionofreturns;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
