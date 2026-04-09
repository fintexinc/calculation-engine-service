package com.fintex.ce.domain.model.result;

import com.fintex.ce.domain.dto.calculation.InflationDTO;

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
public class InflationResult extends WarningResult {

  private Map<String, InflationDTO> inflationData;
}