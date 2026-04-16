package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import com.fintex.ce.model.domain.calculation.distribution.DistributionData;
import com.fintex.ce.model.domain.enumeration.DailyResultType;

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
public class DistributionResDTO extends WarningDTO {
  private Map<DailyResultType, TreeMap<LocalDate, DistributionData>> distribution = new HashMap<>();
}
