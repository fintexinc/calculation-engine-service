package com.fintex.ce.adapter.rest.dto.response.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
public class KeyValueDTO<T> {
  private T key;
  private BigDecimal value;
}
