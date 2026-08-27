package ca.tangerine.pce.model.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import ca.tangerine.wm.commons.domain.holding.HoldingType;
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Command for finding top common holdings across portfolio funds. Supports metric: top-common-holdings")
public class TopCommonHoldingsCommand extends PortfolioHoldingsCommand {
  @Schema(description = "Maximum number of top common holdings to return")
  private Integer numOfTopCommonHoldings;
  @Schema(description = "Holding types to accumulate in the analysis; defaults to the configured set when absent")
  private Set<HoldingType> accumulateHoldingTypes;
}
