package com.fintex.ce.adapter.rest.dto.holding;

import com.fintex.ce.adapter.rest.dto.correlation.HoldingsKeyDTO;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TopCommonHoldingsDTO {

  private String name;
  @JsonIgnore
  private String companyName;
  private String ticker;
  private String exchangeCode;
  private String holdingType;
  private BigDecimal allocation;
  private int numOfFunds;
  private Set<HoldingsKeyDTO> parentHolding;
}
