package com.fintex.ce.model.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "Command for yield calculation. Supports metric: yield")
public class YieldCommand extends PortfolioHoldingsCommand {
  @Schema(description = "Yield calculation time period in months", example = "12")
  private Integer timeIntervalPeriods;
}
