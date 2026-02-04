package com.fintex.ce.application.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class BestWorstPeriodsCommand extends ReturnCommand {
  private Set<Long> bestWorstTimeIntervalPeriods;
}
