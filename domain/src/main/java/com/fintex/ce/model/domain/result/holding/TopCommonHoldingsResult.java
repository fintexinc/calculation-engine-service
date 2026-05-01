package com.fintex.ce.model.domain.result.holding;

import com.fintex.ce.model.domain.result.BaseCalculationResult;

import com.fasterxml.jackson.annotation.JsonInclude;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response for top-common-holdings metric. Contains top common holdings shared across portfolio funds.")
public class TopCommonHoldingsResult extends BaseCalculationResult {

  @Schema(description = "Top common holdings shared across portfolio funds")
  private List<TopCommonHoldingData> commonHoldings;
}
