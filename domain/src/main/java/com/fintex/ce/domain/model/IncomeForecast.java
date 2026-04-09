package com.fintex.ce.domain.model;

import java.math.BigDecimal;
import java.util.List;
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
public class IncomeForecast extends BaseCalculationData<IncomeForecast> {

  private BigDecimal dividendYield;
  private String paymentFrequencyType;
  private List<String> schedule;
  private String maturityDate;
  private String issueDate;

}
