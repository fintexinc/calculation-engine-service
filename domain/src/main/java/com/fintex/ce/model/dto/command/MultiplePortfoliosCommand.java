package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "Command for cross-portfolio analysis with multiple portfolios. Supports metric: common-performance-dates")
public class MultiplePortfoliosCommand extends CalculationCommand implements BenchmarkHoldingsProvider {
  @Schema(description = "Set of portfolios to compare")
  @NotEmpty(message = ErrorCode.Codes.FIELD_NOT_EMPTY)
  private Set<Portfolio> portfolios;
  @Schema(description = "Benchmark holdings for comparison")
  private List<PortfolioHolding> benchmarkHoldings;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Portfolio {
    private List<PortfolioHolding> holdings;
  }
}