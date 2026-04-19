package com.fintex.ce.adapter.rest.dto.returns;

import com.fintex.ce.adapter.rest.dto.WarningDTO;
import com.fintex.ce.model.domain.holding.PortfolioHolding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DailyGrowthOf10KDTO extends WarningDTO {
  private Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> dailyGrowthOf10K = new HashMap<>();
  private LocalDate performanceStartDate;
  private LocalDate performanceEndDate;
}
