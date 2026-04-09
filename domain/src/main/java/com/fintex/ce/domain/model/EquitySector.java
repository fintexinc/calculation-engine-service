package com.fintex.ce.domain.model;

import com.fintex.sm.model.domain.enumeration.EquitySectorAllocationType;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class EquitySector extends BaseCalculationData<EquitySector> {

  private Map<EquitySectorAllocationType, BigDecimal> allocations;

  public EquitySector(Map<EquitySectorAllocationType, BigDecimal> allocations) {
    this.allocations = allocations;
  }

}
