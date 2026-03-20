package com.fintex.ce.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fintex.ce.domain.model.Income;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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