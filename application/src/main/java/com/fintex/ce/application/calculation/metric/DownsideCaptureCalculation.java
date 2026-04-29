package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.UpDownSideCalculationAbstract;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.DownsideCaptureResult;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static java.math.BigDecimal.ZERO;

public class DownsideCaptureCalculation extends UpDownSideCalculationAbstract<DownsideCaptureResult> {

  public DownsideCaptureCalculation(final BenchmarkPeriodCalculationInput input,
      final Set<String> periods) {
    super(input, periods);
  }

  @Override
  public DownsideCaptureResult defineResponseType(Set<Pair<String, BigDecimal>> periodValues) {
    final DownsideCaptureResult result = new DownsideCaptureResult();
    final Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(periodValues);
    result.setDownsideCapture(timeIntervals);
    return result;
  }

  @Override
  public boolean filterCaptureExpression(final Map.Entry<LocalDate, BigDecimal> e) {
    return ZERO.compareTo(e.getValue()) > 0;
  }

}
