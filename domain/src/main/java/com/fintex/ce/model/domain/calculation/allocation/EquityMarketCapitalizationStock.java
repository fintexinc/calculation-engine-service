package com.fintex.ce.model.domain.calculation.allocation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EquityMarketCapitalizationStock extends HoldingEquityMarketCap {

  private String styleBox;

}
