package com.fintex.ce.model.domain.result;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RollingIntervalResult {

  private String timeIntervalPeriod;
  private Set<IntervalResult> values;
}
