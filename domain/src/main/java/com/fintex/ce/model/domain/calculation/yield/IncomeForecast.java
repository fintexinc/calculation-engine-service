package com.fintex.ce.model.domain.calculation.yield;

import com.fintex.ce.model.domain.calculation.BaseCalculationData;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class IncomeForecast extends BaseCalculationData<IncomeForecast> {

  private BigDecimal dividendYield;
  private String paymentFrequencyType;
  private List<String> schedule;
  private String maturityDate;
  private String issueDate;

}
