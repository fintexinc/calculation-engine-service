package com.fintex.ce.domain.model.result;

import com.fintex.ce.domain.model.calculation.DistributionData;
import com.fintex.ce.domain.model.enumeration.DailyResultType;

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