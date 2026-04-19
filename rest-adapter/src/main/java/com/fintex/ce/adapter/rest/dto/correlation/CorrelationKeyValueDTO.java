package com.fintex.ce.adapter.rest.dto.correlation;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CorrelationKeyValueDTO {

  private String correlationKey;
  private BigDecimal value;

}
