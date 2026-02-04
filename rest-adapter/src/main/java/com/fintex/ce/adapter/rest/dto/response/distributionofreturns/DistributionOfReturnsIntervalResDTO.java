package com.fintex.ce.adapter.rest.dto.response.distributionofreturns;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

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
