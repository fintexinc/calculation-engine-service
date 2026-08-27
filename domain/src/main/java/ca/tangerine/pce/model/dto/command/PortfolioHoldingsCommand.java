package ca.tangerine.pce.model.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.dto.command.contract.HoldingsProvider;
import ca.tangerine.pce.model.error.ErrorCode;
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Command for portfolio breakdown and allocation calculations. Supports metrics: asset-allocations, asset-allocations-em, equity-sector, equity-country-exposure, equity-stylebox-exposure, equity-geographic-exposure, equity-market-capitalization, fixed-income-country-exposure, fixed-income-geographic-exposure, fixed-income-bond-sector, fixed-income-stylebox-exposure, maturity-allocation, classification-allocation, sales-charge, fixed-income-credit-quality")
public class PortfolioHoldingsCommand extends CalculationCommand implements HoldingsProvider {
  @Schema(description = "Portfolio holdings to analyze; in a composite request they may come from the shared top level")
  @NotEmpty(message = ErrorCode.Codes.FIELD_NOT_EMPTY)
  protected List<PortfolioHolding> holdings;
}
