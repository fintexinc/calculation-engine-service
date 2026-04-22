package com.fintex.ce.model.domain.result;

import com.fintex.ce.model.dto.calculation.InflationDTO;

import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class InflationResult extends BaseCalculationResult {

  private Map<String, InflationDTO> inflationData;
}