package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.domain.model.enumeration.DailyResultType;
import com.fintex.ce.domain.model.calculation.DistributionData;
import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DistributionResDTO extends WarningDTO {
  private Map<DailyResultType, TreeMap<LocalDate, DistributionData>> distribution = new HashMap<>();
}
