package com.fintex.ce.model.domain.calculation.input;

import com.fintex.ce.model.error.Warning;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NavigableMap;
import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class PeriodCalculationInput {

  public PeriodCalculationInput(
      final LocalDate cipsd,
      final NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns) {
    this.cipsd = cipsd;
    this.weightedAveragePortfolioReturns = weightedAveragePortfolioReturns;
  }

  public PeriodCalculationInput(
      final LocalDate cipsd,
      final NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns,
      final List<Warning> warnings) {
    this.cipsd = cipsd;
    this.weightedAveragePortfolioReturns = weightedAveragePortfolioReturns;
    this.warnings = warnings;
  }

  public PeriodCalculationInput(final NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns) {
    this.weightedAveragePortfolioReturns = weightedAveragePortfolioReturns;
  }

  public PeriodCalculationInput() {
  }

  // custom interval portfolio start date
  protected LocalDate cipsd;
  private NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns;
  private List<Warning> warnings;
}
