package com.fintex.ce.domain.model.result;

import com.fintex.ce.domain.model.result.correlation.CorrelationPeriodResult;
import com.fintex.ce.domain.model.result.correlation.HoldingsKeyResult;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class CorrelationResult extends PeriodResult {

  private List<HoldingsKeyResult> holdingsKey;
  private List<CorrelationPeriodResult> correlationPeriods;
}
