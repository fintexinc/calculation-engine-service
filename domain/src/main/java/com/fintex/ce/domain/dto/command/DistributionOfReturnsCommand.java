package com.fintex.ce.domain.dto.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class DistributionOfReturnsCommand extends PeriodCommand {
  private LocalDate customPsd;
  private Integer customNumberOfBins;
}
