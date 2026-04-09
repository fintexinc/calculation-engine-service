package com.fintex.ce.domain.model.result;

import com.fintex.ce.domain.model.result.core.TimeIntervalResult;

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
public class AlphaResult extends PeriodResult {

  private Set<TimeIntervalResult> alpha;
}
