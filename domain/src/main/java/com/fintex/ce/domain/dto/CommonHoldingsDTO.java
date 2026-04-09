package com.fintex.ce.domain.dto;

import com.fintex.ce.domain.model.HoldingAggregator;
import com.fintex.ce.domain.model.holding.Holding;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CommonHoldingsDTO {

  private static final String EQUITY_TYPE = "E";

  private String name;
  private String companyName;
  private String type;
  private BigDecimal value;
  private List<CommonHoldingsDTO> underlyingHoldings;
  private String ticker;
  private String exchangeCode;

  private Holding holding;
  private BigDecimal weight;

  // This field is using only for identifying GIC holdings, as it is possible that all parameters of GIC are equals but
  // them aren't same
  private UUID uuid = null;

  public CommonHoldingsDTO(final String companyName, final String type, final BigDecimal value,
      final String ticker, final String exchangeCode) {
    this.companyName = companyName;
    this.type = type;
    this.value = value;
    this.ticker = ticker;
    this.exchangeCode = exchangeCode;
  }

  public HoldingAggregator aggregator() {
    if (EQUITY_TYPE.equalsIgnoreCase(type) && companyName != null && !companyName.isEmpty()) {
      return new HoldingAggregator(null, companyName, uuid);
    }
    return new HoldingAggregator(name, null, uuid);
  }

}
