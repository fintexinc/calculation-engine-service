package com.fintex.ce.adapter.rest.dto.response.correlation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CorrelationKeyValueDTO {

  private String correlationKey;
  private BigDecimal value;

}
