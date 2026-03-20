package com.fintex.ce.domain.model.result;

import com.fintex.ce.domain.model.result.distributionofreturns.DistributionOfReturnsIntervalResult;
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
