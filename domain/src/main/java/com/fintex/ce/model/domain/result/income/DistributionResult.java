package com.fintex.ce.model.domain.result.income;

import com.fintex.ce.model.domain.calculation.distribution.DistributionData;
import com.fintex.ce.model.domain.enumeration.DailyResultType;
import com.fintex.ce.model.domain.result.BaseCalculationResult;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DistributionResult extends BaseCalculationResult {
  @Builder.Default
  private Map<DailyResultType, TreeMap<LocalDate, DistributionData>> distribution = new HashMap<>();
}