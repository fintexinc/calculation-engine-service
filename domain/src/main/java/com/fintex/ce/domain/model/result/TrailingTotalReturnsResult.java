package com.fintex.ce.domain.model.result;

import com.fintex.ce.domain.model.result.core.TimeIntervalResult;
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
public class TrailingTotalReturnsResult extends PeriodResult {

  private Set<TimeIntervalResult> trailingTotalReturn;
}
