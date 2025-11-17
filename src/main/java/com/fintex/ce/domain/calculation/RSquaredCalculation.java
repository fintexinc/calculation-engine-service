package com.fintex.ce.domain.calculation;


import com.fintex.ce.domain.calculation.core.RSquaredCalculationAbstract;
import com.fintex.ce.dto.RSquaredResDTO;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.response.core.TimeIntervalResDTO;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

@Getter
public class RSquaredCalculation extends RSquaredCalculationAbstract<RSquaredResDTO> {

    public RSquaredCalculation(final BenchmarkCalculationDTO input,
                               final Set<String> periods,
                               final NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn,
                               final NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn) {
        super(input, periods, portfolioExcessReturn, benchmarkExcessReturn);
    }

    @Override
    public RSquaredResDTO defineResponseType(final Set<Pair<String, BigDecimal>> result) {
        final RSquaredResDTO resDTO = new RSquaredResDTO();
        final Set<TimeIntervalResDTO> timeIntervals = formTimeIntervalResDTO(result);
        resDTO.setRSquared(timeIntervals);
        return resDTO;
    }

}
