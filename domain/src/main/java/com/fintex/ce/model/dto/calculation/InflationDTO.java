package com.fintex.ce.model.dto.calculation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.TreeMap;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class InflationDTO {

  private TreeMap<LocalDate, BigDecimal> monthlyInflationChange;
  private TreeMap<LocalDate, BigDecimal> inflation;

}
