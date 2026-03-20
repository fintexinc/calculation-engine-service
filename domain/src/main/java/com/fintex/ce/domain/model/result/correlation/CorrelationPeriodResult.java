package com.fintex.ce.domain.model.result.correlation;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CorrelationPeriodResult {

  private String period;
  private String key;
  private List<CorrelationKeyValueResult> correlations;
}
