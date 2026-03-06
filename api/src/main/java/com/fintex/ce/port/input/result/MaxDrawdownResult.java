package com.fintex.ce.port.input.result;

import com.fintex.ce.port.input.result.core.MaxDrawdownEntry;
import com.fintex.ce.port.input.result.PeriodResult;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class MaxDrawdownResult extends PeriodResult {

  private List<MaxDrawdownEntry> maxDrawdown;
}
