package com.fintex.ce.port.input.result;

import com.fintex.ce.port.input.result.correlation.CorrelationPeriodResult;
import com.fintex.ce.port.input.result.correlation.HoldingsKeyResult;
import com.fintex.ce.port.input.result.PeriodResult;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class CorrelationResult extends PeriodResult {

  private List<HoldingsKeyResult> holdingsKey;
  private List<CorrelationPeriodResult> correlationPeriods;
}
