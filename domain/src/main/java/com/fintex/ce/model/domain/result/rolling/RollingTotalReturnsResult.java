package com.fintex.ce.model.domain.result.rolling;

import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.domain.result.RollingIntervalResult;

import java.util.Set;
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
public class RollingTotalReturnsResult extends PeriodResult {

  private Set<RollingIntervalResult> rollingTotalReturns;
}
