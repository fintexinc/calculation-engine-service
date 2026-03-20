package com.fintex.ce.domain.dto.calculation;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.TreeMap;

@Data
@ToString(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class InflationDTO {

  private TreeMap<LocalDate, BigDecimal> monthlyInflationChange;
  private TreeMap<LocalDate, BigDecimal> inflation;

}
