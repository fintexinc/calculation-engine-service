package com.fintex.ce.model.domain.result.correlation;

import com.fintex.ce.model.domain.result.PeriodResult;

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
