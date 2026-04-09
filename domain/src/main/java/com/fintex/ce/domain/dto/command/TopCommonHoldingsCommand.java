package com.fintex.ce.domain.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "Command for finding top common holdings across portfolio funds. Supports metric: top-common-holdings")
public class TopCommonHoldingsCommand extends PortfolioHoldingsCommand {
  @Schema(description = "Minimum number of funds a holding must appear in")
  private Integer numOfFundsMin;
  @Schema(description = "Maximum number of top common holdings to return")
  private Integer numOfTopCommonHoldings;
  @Schema(description = "Holding types to accumulate in the analysis")
  private Set<String> accumulateHoldingTypes;
}
