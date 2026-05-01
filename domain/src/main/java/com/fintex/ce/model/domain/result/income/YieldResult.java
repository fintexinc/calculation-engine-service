package com.fintex.ce.model.domain.result.income;

import com.fintex.ce.model.domain.result.BaseCalculationResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for yield metric. Contains portfolio yield value.")
public class YieldResult extends BaseCalculationResult {

  @Schema(description = "Portfolio yield value")
  private BigDecimal yield;
}
