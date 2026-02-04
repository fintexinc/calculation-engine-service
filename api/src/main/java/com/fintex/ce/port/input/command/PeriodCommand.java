package com.fintex.ce.port.input.command;

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
public class PeriodCommand extends PortfolioCommand {
  private LocalDate customIntervalPsd;
  private LocalDate customPed;
  private Set<String> periods;
}
