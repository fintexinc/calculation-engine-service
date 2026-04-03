package com.fintex.ce.domain.dto.command;

import com.fintex.sm.model.domain.enumeration.CurrencyType;
import com.fintex.ce.domain.model.holding.Holding;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(description = "Command for return-based calculations with custom date range. Supports metrics: annual-return, growth-of-10k")
public class ReturnCommand extends CalculationCommand {
  @Schema(description = "Custom performance start date")
  private LocalDate customPerformanceStartDate;
  @Schema(description = "Custom performance end date")
  private LocalDate customPerformanceEndDate;
  @Schema(description = "Target currency", example = "CAD")
  private CurrencyType currency;
  @Schema(description = "Portfolio holdings")
  private List<Holding> holdings;
}
