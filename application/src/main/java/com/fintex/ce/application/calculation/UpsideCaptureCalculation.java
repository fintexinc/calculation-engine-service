package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.core.UpDownSideCalculationAbstract;
import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.domain.model.result.UpsideCaptureResult;
import com.fintex.ce.domain.model.result.core.TimeIntervalResult;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static java.math.BigDecimal.ZERO;

public class UpsideCaptureCalculation extends UpDownSideCalculationAbstract<UpsideCaptureResult> {

  public UpsideCaptureCalculation(final BenchmarkCalculationDTO input,
      final Set<String> periods) {
    super(input, periods);
  }

  @Override
  public UpsideCaptureResult defineResponseType(final Set<Pair<String, BigDecimal>> result) {
    final UpsideCaptureResult resDTO = new UpsideCaptureResult();
    final Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(result);
    resDTO.setUpsideCapture(timeIntervals);
    return resDTO;
  }

  @Override
  public boolean filterCaptureExpression(final Map.Entry<LocalDate, BigDecimal> e) {
    return ZERO.compareTo(e.getValue()) < 0;
  }
}
