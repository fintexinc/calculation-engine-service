package com.fintex.ce.domain.dto;

import com.fintex.ce.domain.model.Income;

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
public class IncomeForecastDto {

  private String type;
  private String holdingIdentifier;
  private String exchangeCode;
  private String identifier;
  private String ticker;
  private String fundServeCode;
  private List<Income> income;
}