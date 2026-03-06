package com.fintex.ce.port.input.result.distributionofreturns;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DistributionRangeResult {

  private int bin;
  private BigDecimal range;
  private long value;
}
