package com.fintex.ce.application.command;

import com.fintex.ce.port.input.command.PeriodCommand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class RollingCalculationCommand extends PeriodCommand {
  private LocalDate customPsd;
  private Set<String> rollingPeriods;
}
