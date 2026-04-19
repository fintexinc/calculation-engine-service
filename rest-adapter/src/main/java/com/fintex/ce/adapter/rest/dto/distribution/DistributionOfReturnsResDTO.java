package com.fintex.ce.adapter.rest.dto.distribution;

import com.fintex.ce.adapter.rest.dto.PeriodResDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for distribution-of-monthly-return metric. Contains distribution histogram of monthly and yearly returns.")
public class DistributionOfReturnsResDTO extends PeriodResDTO {

  @Schema(description = "Distribution of monthly returns")
  private DistributionOfReturnsIntervalResDTO monthlyReturns;
  @Schema(description = "Distribution of yearly returns")
  private DistributionOfReturnsIntervalResDTO yearlyReturns;

}
