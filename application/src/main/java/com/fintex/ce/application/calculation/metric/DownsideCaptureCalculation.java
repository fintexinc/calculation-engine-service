package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.UpDownSideCalculationAbstract;
import com.fintex.ce.domain.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.domain.model.result.DownsideCaptureResult;
import com.fintex.ce.domain.model.result.core.TimeIntervalResult;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static java.math.BigDecimal.ZERO;

public class DownsideCaptureCalculation extends UpDownSideCalculationAbstract<DownsideCaptureResult> {

  public DownsideCaptureCalculation(final BenchmarkCalculationDTO input,
      final Set<String> periods) {
    super(input, periods);
  }

  @Override
  public DownsideCaptureResult defineResponseType(Set<Pair<String, BigDecimal>> result) {
    final DownsideCaptureResult resDto = new DownsideCaptureResult();
    final Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(result);
    resDto.setDownsideCapture(timeIntervals);
    return resDto;
  }

  @Override
  public boolean filterCaptureExpression(final Map.Entry<LocalDate, BigDecimal> e) {
    return ZERO.compareTo(e.getValue()) > 0;
  }

}
