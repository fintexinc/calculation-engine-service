package com.fintex.ce.domain.dto.command;

import com.fintex.ce.domain.model.holding.Holding;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MultiplePortfoliosCommand implements CalculationCommand {
  private Set<Portfolio> portfolios;
  private List<Holding> benchmarkHoldings;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Portfolio {
    private List<Holding> holdings;
  }
}