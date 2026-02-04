package com.fintex.ce.application.result;

import com.fintex.ce.application.result.core.TimeIntervalResult;
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
public class InformationRatioResult extends PeriodResult {

  private Set<TimeIntervalResult> timeIntervalResultS;
}
