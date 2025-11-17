package com.fintex.ce.domain.calculation.core;

import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.response.core.PeriodResDTO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

@Getter
public abstract class BenchmarkWeightedAverageCalculation<T extends PeriodResDTO, V> extends PeriodCalculationAbstract<T, V> {

    @Setter
    protected NavigableMap<LocalDate, BigDecimal> benchmarkTotalReturns;

    protected BenchmarkWeightedAverageCalculation(final BenchmarkCalculationDTO input,
                                               final Set<String> periods) {
        super(input, periods);
        this.benchmarkTotalReturns = input.getWeightedAverageBenchmarkReturns();
    }

}
