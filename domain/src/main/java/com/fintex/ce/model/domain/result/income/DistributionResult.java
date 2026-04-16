package com.fintex.ce.model.domain.result.income;

import com.fintex.ce.model.domain.calculation.distribution.DistributionData;
import com.fintex.ce.model.domain.enumeration.DailyResultType;
import com.fintex.ce.model.domain.result.WarningResult;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class DistributionResult extends WarningResult {
  private Map<DailyResultType, TreeMap<LocalDate, DistributionData>> distribution = new HashMap<>();
}