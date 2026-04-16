package com.fintex.ce.model.domain.result.holding;

import com.fintex.ce.model.domain.result.correlation.HoldingsKeyResult;

import java.math.BigDecimal;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
