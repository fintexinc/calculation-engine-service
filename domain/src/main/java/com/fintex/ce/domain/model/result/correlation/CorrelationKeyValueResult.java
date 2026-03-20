package com.fintex.ce.domain.model.result.correlation;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CorrelationKeyValueResult {

  private String correlationKey;
  private BigDecimal value;
}
