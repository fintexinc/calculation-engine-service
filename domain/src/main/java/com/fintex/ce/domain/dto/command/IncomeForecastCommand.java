package com.fintex.ce.domain.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "Command for income forecast calculation. Supports metric: income-forecast")
public class IncomeForecastCommand extends PortfolioHoldingsCommand {
  @Schema(description = "Forecast time period in months", example = "12")
  private Integer timeIntervalPeriods;
}
