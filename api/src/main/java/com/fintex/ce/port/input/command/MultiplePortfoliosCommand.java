package com.fintex.ce.port.input.command;

import com.fintex.ce.domain.model.holding.Holding;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;

@Data
@Accessors(chain = true)
public class MultiplePortfoliosCommand {
  private Set<Portfolio> portfolios;
  private List<Holding> benchmarkHoldings;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Portfolio {
    private List<Holding> holdings;
  }
}