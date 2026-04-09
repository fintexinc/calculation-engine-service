package com.fintex.ce.domain.model.result.correlation;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CorrelationPeriodResult {

  private String period;
  private String key;
  private List<CorrelationKeyValueResult> correlations;
}
