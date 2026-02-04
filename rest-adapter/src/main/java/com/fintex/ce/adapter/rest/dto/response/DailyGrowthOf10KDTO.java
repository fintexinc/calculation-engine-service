package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DailyGrowthOf10KDTO extends WarningDTO {
  private Map<Holding, TreeMap<LocalDate, BigDecimal>> dailyGrowthOf10K = new HashMap<>();
  private LocalDate performanceStartDate;
  private LocalDate performanceEndDate;
}
