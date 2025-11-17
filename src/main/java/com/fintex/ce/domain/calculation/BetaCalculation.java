package com.fintex.ce.domain.calculation;

import com.fintex.ce.domain.calculation.core.AlphaBetaCalculationAbstract;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.response.BetaResDTO;
import com.fintex.ce.dto.response.core.TimeIntervalResDTO;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

@Getter
public class BetaCalculation extends AlphaBetaCalculationAbstract<BetaResDTO> {

    public BetaCalculation(final BenchmarkCalculationDTO input,
                           final Set<String> periods,
                           final NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn,
                           final NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn) {
        super(input, periods, portfolioExcessReturn, benchmarkExcessReturn);
    }

    @Override
    public BetaResDTO defineResponseType(final Set<Pair<String, BigDecimal>> result) {
        final BetaResDTO resDTO = new BetaResDTO();
        final Set<TimeIntervalResDTO> timeIntervals = formTimeIntervalResDTO(result);
        resDTO.setBeta(timeIntervals);
        return resDTO;
    }

}
