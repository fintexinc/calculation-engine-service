package com.fintex.ce.port.input.result;

import com.fintex.ce.port.input.result.core.RollingIntervalResult;
import com.fintex.ce.port.input.result.PeriodResult;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class RollingTotalReturnsResult extends PeriodResult {

  private Set<RollingIntervalResult> rollingTotalReturns;
}
