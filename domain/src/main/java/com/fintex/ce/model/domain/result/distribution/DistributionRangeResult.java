package com.fintex.ce.model.domain.result.distribution;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DistributionRangeResult {

  private int bin;
  private BigDecimal range;
  private long value;
}
