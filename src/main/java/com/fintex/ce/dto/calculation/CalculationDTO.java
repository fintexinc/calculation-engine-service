package com.fintex.ce.dto.calculation;

import com.fintex.ce.dto.response.core.Warning;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NavigableMap;

@Data
@Accessors(chain = true)
public class CalculationDTO {

    public CalculationDTO(
            final LocalDate cipsd,
            final NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns
    ) {
        this.cipsd = cipsd;
        this.weightedAveragePortfolioReturns = weightedAveragePortfolioReturns;
    }

    public CalculationDTO(
            final LocalDate cipsd,
            final NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns,
            final List<Warning> warnings
    ) {
        this.cipsd = cipsd;
        this.weightedAveragePortfolioReturns = weightedAveragePortfolioReturns;
        this.warnings = warnings;
    }

    public CalculationDTO() {
    }

    // custom interval portfolio start date
    protected LocalDate cipsd;
    private NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns;
    private List<Warning> warnings;
}
