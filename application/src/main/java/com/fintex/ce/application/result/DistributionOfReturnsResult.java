package com.fintex.ce.application.result;

import com.fintex.ce.application.result.distributionofreturns.DistributionOfReturnsIntervalResult;
import com.fintex.ce.port.input.result.PeriodResult;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class DistributionOfReturnsResult extends PeriodResult {

  private DistributionOfReturnsIntervalResult monthlyReturns;
  private DistributionOfReturnsIntervalResult yearlyReturns;
}
