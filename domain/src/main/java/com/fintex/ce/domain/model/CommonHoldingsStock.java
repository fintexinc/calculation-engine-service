package com.fintex.ce.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CommonHoldingsStock extends CommonHoldings {

  private String companyName;
  private String ticker;
  private String exchangeCode;

  public CommonHoldingsStock(String companyName, String ticker, String exchangeCode) {
    this.companyName = companyName;
    this.ticker = ticker;
    this.exchangeCode = exchangeCode;
  }

}
