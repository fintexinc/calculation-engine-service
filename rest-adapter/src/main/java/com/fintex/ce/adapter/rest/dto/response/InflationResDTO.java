package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.domain.model.calculation.InflationDTO;
import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
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
public class InflationResDTO extends WarningDTO {

  private Map<String, InflationDTO> inflationData;

}
