package com.fintex.ce.model.dto.command;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Command for leading (forward-looking) total return calculation. Supports metric: leading-total-return")
public class LeadingTotalReturnCommand extends PeriodCommand implements CustomPsdProvider {
  @Schema(description = "Custom performance start date")
  @JsonProperty("customPerformanceStartDate")
  private LocalDate customPsd;
}
