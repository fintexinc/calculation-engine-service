package com.fintex.ce.dto.calculation;

import com.fintex.ce.dto.holding.Holding;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
public class BenchmarkMReturnsDTO {

    private Map<Holding, Map<LocalDate, BigDecimal>> portfolioReturns;
    private Map<Holding, Map<LocalDate, BigDecimal>> benchmarkReturns;

    public BenchmarkMReturnsDTO() {
        portfolioReturns = new HashMap<>();
        benchmarkReturns = new HashMap<>();
    }

    public BenchmarkMReturnsDTO(Map<Holding, Map<LocalDate, BigDecimal>> portfolioReturns,
                                Map<Holding, Map<LocalDate, BigDecimal>> benchmarkReturns) {
        this.portfolioReturns = portfolioReturns;
        this.benchmarkReturns = benchmarkReturns;
    }
}
