package com.fintex.ce.domain.model;

import com.fintex.ce.domain.enumeration.calculation.EquityStyleboxType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquityStyleboxExposureDTO {

  private EquityStyleboxType boxType;
  private BigDecimal value;

}
