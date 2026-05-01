package com.fintex.ce.model.domain.calculation.yield;

import com.fintex.ce.model.domain.calculation.BaseCalculationData;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Yield extends BaseCalculationData<Yield> {

  private BigDecimal dividendYield;

}
