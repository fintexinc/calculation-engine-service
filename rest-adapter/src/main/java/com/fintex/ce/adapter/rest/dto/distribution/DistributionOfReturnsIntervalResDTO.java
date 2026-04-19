package com.fintex.ce.adapter.rest.dto.distribution;

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
public class DistributionOfReturnsIntervalResDTO {

  private BigDecimal distributionMin;
  private BigDecimal distributionMax;
  private int distributionBin;
  private BigDecimal distributionIncrement;
  private List<DistributionRangeResDTO> distributionRange;

}
