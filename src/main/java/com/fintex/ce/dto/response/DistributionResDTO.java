package com.fintex.ce.dto.response;

import com.fintex.ce.config.enumeration.DailyResultType;
import com.fintex.ce.dto.calculation.DistributionData;
import com.fintex.ce.dto.response.core.WarningDTO;
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
