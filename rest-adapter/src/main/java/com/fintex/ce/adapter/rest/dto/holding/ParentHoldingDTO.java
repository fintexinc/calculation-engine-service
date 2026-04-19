package com.fintex.ce.adapter.rest.dto.holding;

import com.fintex.ce.adapter.rest.dto.correlation.HoldingsKeyDTO;
import com.fintex.ce.model.domain.holding.PortfolioHolding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonIgnoreProperties({"key"})
public class ParentHoldingDTO extends HoldingsKeyDTO {

  public static HoldingsKeyDTO buildDTO(final PortfolioHolding holding, final BigDecimal allocation) {
    return buildParentKeyDTO(holding).setAllocation(allocation);
  }
}