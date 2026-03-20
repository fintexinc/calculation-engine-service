package com.fintex.ce.adapter.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintex.ce.domain.dto.calculation.HoldingForDailyCalculationDTO;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DailyPerformanceReqDTO {
  @JsonProperty("startDate")
  private LocalDate startDate;

  @JsonProperty("endDate")
  private LocalDate endDate;

  @JsonProperty("dailyHoldings")
  private List<HoldingForDailyCalculationDTO> dailyHoldings;

}
