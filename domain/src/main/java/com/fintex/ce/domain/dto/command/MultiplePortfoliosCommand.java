package com.fintex.ce.domain.dto.command;

import com.fintex.ce.domain.model.holding.Holding;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(description = "Command for cross-portfolio analysis with multiple portfolios. Supports metric: common-performance-dates")
public class MultiplePortfoliosCommand extends CalculationCommand {
  @Schema(description = "Set of portfolios to compare")
  private Set<Portfolio> portfolios;
  @Schema(description = "Benchmark holdings for comparison")
  private List<Holding> benchmarkHoldings;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Portfolio {
    private List<Holding> holdings;
  }
}