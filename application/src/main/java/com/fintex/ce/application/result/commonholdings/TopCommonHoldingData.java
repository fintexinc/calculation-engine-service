package com.fintex.ce.application.result.commonholdings;

import com.fintex.ce.application.result.correlation.HoldingsKeyResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@lombok.experimental.Accessors(chain = true)
public class TopCommonHoldingData {

  private String name;
  private String ticker;
  private String exchangeCode;
  private String holdingType;
  private BigDecimal allocation;
  private int numOfFunds;
  private Set<HoldingsKeyResult> parentHolding;
}
