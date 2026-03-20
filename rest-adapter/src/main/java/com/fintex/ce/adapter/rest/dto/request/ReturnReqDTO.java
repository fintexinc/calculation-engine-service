package com.fintex.ce.adapter.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.holding.Holding;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ReturnReqDTO {

  @JsonProperty(value = "customPerformanceStartDate")
  private LocalDate customPerformanceStartDate;

  @JsonProperty(value = "customPerformanceEndDate")
  private LocalDate customPerformanceEndDate;

  @JsonProperty(value = "currency")
  private Currency currency;

  @JsonProperty(value = "holdings")
  private List<Holding> holdings;

}
