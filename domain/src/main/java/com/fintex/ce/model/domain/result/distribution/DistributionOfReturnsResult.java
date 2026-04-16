package com.fintex.ce.model.domain.result.distribution;

import com.fintex.ce.model.domain.result.PeriodResult;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class DistributionOfReturnsResult extends PeriodResult {

  private DistributionOfReturnsIntervalResult monthlyReturns;
  private DistributionOfReturnsIntervalResult yearlyReturns;
}
