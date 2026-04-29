package com.fintex.ce.model.domain.calculation.yield;

import com.fintex.ce.model.domain.calculation.distribution.Income;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HoldingIncomeForecast {

  private String type;
  private String holdingIdentifier;
  private String exchangeCode;
  private String identifier;
  private String ticker;
  private String fundServeCode;
  private List<Income> income;
}