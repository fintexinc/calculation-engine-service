package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.UpDownSideCalculationAbstract;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.result.risk.UpsideCaptureResult;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static java.math.BigDecimal.ZERO;

public class UpsideCaptureCalculation extends UpDownSideCalculationAbstract<UpsideCaptureResult> {

  public UpsideCaptureCalculation(final BenchmarkPeriodCalculationInput input,
      final Set<TimePeriod> periods) {
    super(input, periods);
  }

  @Override
  public UpsideCaptureResult defineResponseType(final Map<String, BigDecimal> periodValues) {
    return new UpsideCaptureResult(periodValues);
  }

  @Override
  public boolean filterCaptureExpression(final Map.Entry<LocalDate, BigDecimal> e) {
    return ZERO.compareTo(e.getValue()) < 0;
  }
}
