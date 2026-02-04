package com.fintex.ce.port.input.result;

import com.fintex.ce.domain.enumeration.DailyResultType;
import com.fintex.ce.domain.model.calculation.DistributionData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class DistributionResult extends WarningResult {
  private Map<DailyResultType, TreeMap<LocalDate, DistributionData>> distribution = new HashMap<>();
}