package com.fintex.ce.port.input.command;

import com.fintex.ce.port.input.command.PeriodCommand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class LeadingTotalReturnCommand extends PeriodCommand {
  private LocalDate customPsd;
}
