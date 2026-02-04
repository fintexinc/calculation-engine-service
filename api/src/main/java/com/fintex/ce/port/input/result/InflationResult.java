package com.fintex.ce.port.input.result;

import com.fintex.ce.domain.model.calculation.InflationDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class InflationResult extends WarningResult {

  private Map<String, InflationDTO> inflationData;
}