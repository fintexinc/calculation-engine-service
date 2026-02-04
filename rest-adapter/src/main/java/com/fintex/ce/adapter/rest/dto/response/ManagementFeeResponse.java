package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.domain.enumeration.ParameterType;
import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ManagementFeeResponse extends WarningDTO {

  private Map<ParameterType, BigDecimal> managementFee = new HashMap<>();

}
