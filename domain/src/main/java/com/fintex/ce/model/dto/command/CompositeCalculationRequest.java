package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for calculating several metrics in one call. Portfolio holdings, data providers and target currency are
 * declared once at the top level and shared by every nested command; a nested command may still override any of them by
 * carrying its own value. Metric-specific parameters (periods, benchmark holdings, fee modes, ...) stay on the nested
 * commands.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Composite calculation request with shared portfolio inputs and one command per metric")
public class CompositeCalculationRequest {

  @Schema(description = "Portfolio holdings shared by all commands; a command may override with its own holdings")
  private List<PortfolioHolding> holdings;

  @ArraySchema(arraySchema = @Schema(description = "Data providers shared by all commands; configured defaults apply when absent", example = "[\"MORNINGSTAR\"]"), schema = @Schema(implementation = DataProvider.class))
  private List<DataProvider> dataProviders;

  @Schema(description = "Target currency shared by all currency-aware commands", example = "CAD")
  private Currency currency;

  @Schema(description = "Commands to execute, each carrying its metric and metric-specific parameters")
  @NotEmpty(message = ErrorCode.Codes.FIELD_NOT_EMPTY)
  private List<CalculationCommand> commands;
}
