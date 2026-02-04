package com.fintex.ce.domain.model;

import com.fintex.ce.domain.enumeration.calculation.MaturityAllocationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaturityAllocationDTO {

  private MaturityAllocationType boxType;
  private BigDecimal value;

}
