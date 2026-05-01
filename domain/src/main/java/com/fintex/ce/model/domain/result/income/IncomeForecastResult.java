package com.fintex.ce.model.domain.result.income;

import com.fintex.ce.model.domain.calculation.yield.HoldingIncomeForecast;
import com.fintex.ce.model.domain.result.BaseCalculationResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
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
@Schema(description = "Response for income-forecast metric. Contains income forecast for holdings.")
public class IncomeForecastResult extends BaseCalculationResult {

  @Schema(description = "Income forecast for holdings")
  private List<HoldingIncomeForecast> incomeForecast;
}
