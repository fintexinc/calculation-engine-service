package com.fintex.ce.adapter.rest.dto.response.commonholdings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.adapter.rest.dto.response.correlation.HoldingsKeyDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonIgnoreProperties({"key"})
public class ParentHoldingDTO extends HoldingsKeyDTO {

  public static HoldingsKeyDTO buildDTO(final Holding holding, final BigDecimal allocation) {
    return buildParentKeyDTO(holding).setAllocation(allocation);
  }
}