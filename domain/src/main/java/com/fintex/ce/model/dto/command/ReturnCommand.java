package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.currency.Currency;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(description = "Command for return-based calculations with custom date range. Supports metrics: annual-return, growth-of-10k")
public class ReturnCommand extends CalculationCommand
    implements
      HoldingsProvider,
      CustomPsdProvider,
      CustomPedProvider {
  @Schema(description = "Custom performance start date")
  private LocalDate customPsd;
  @Schema(description = "Custom performance end date")
  private LocalDate customPed;
  @NotNull(message = ErrorCode.Codes.FIELD_NOT_NULL)
  @Schema(description = "Target currency", example = "CAD")
  private Currency currency;
  @Schema(description = "Portfolio holdings")
  @NotEmpty(message = ErrorCode.Codes.FIELD_NOT_EMPTY)
  private List<PortfolioHolding> holdings;
}
