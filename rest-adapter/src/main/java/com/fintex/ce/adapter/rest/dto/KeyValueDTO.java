package com.fintex.ce.adapter.rest.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
public class KeyValueDTO<T> {
  private T key;
  private BigDecimal value;
}
