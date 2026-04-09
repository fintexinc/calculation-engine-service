package com.fintex.ce.domain.model.result;

import com.fintex.ce.domain.model.result.core.MaxDrawdownEntry;

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
public class MaxDrawdownResult extends PeriodResult {

  private List<MaxDrawdownEntry> maxDrawdown;
}
