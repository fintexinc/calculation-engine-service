package com.fintex.ce.domain.dto.command;

import com.fintex.ce.domain.model.holding.Holding;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "Command for portfolio breakdown and allocation calculations. Supports metrics: asset-allocations, asset-allocations-em, equity-sector, equity-country-exposure, equity-stylebox-exposure, equity-geographic-exposure, equity-market-capitalization, fixed-income-country-exposure, fixed-income-geographic-exposure, fixed-income-bond-sector, fixed-income-stylebox-exposure, maturity-allocation, classification-allocation, sales-charge, fixed-income-credit-quality")
public class PortfolioHoldingsCommand extends DataProviderCommand {
  @Schema(description = "Portfolio holdings to analyze")
  private List<Holding> holdings;
}
