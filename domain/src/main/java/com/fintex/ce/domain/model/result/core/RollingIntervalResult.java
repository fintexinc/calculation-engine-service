package com.fintex.ce.domain.model.result.core;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RollingIntervalResult {

  private String timeIntervalPeriod;
  private Set<IntervalResult> values;
}
