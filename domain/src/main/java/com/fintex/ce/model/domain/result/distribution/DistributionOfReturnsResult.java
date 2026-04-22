package com.fintex.ce.model.domain.result.distribution;

import com.fintex.ce.model.domain.result.PeriodResult;

import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Response for distribution-of-monthly-return metric. Contains distribution histogram of monthly and yearly returns.")
public class DistributionOfReturnsResult extends PeriodResult {

  @Schema(description = "Distribution of monthly returns")
  private DistributionOfReturnsIntervalResult monthlyReturns;
  @Schema(description = "Distribution of yearly returns")
  private DistributionOfReturnsIntervalResult yearlyReturns;
}
