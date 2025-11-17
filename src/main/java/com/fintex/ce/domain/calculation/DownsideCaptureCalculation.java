package com.fintex.ce.domain.calculation;

import com.fintex.ce.domain.calculation.core.UpDownSideCalculationAbstract;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.response.DownsideCaptureResDTO;
import com.fintex.ce.dto.response.core.TimeIntervalResDTO;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static java.math.BigDecimal.ZERO;

public class DownsideCaptureCalculation extends UpDownSideCalculationAbstract<DownsideCaptureResDTO> {

    public DownsideCaptureCalculation(final BenchmarkCalculationDTO input,
                                      final Set<String> periods) {
        super(input, periods);
    }

    @Override
    public DownsideCaptureResDTO defineResponseType(Set<Pair<String, BigDecimal>> result) {
        final DownsideCaptureResDTO resDto = new DownsideCaptureResDTO();
        final Set<TimeIntervalResDTO> timeIntervals = formTimeIntervalResDTO(result);
        resDto.setDownsideCapture(timeIntervals);
        return resDto;
    }

    @Override
    public boolean filterCaptureExpression(final Map.Entry<LocalDate, BigDecimal> e) {
        return ZERO.compareTo(e.getValue()) > 0;
    }

}
