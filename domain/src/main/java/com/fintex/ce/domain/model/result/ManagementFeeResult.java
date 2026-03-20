package com.fintex.ce.domain.model.result;

import com.fintex.ce.domain.model.enumeration.ParameterType;
import lombok.Data;
import lombok.experimental.Accessors;
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
@Accessors(chain = true)
public class ManagementFeeResult extends WarningResult {

  private Map<ParameterType, BigDecimal> managementFee = new HashMap<>();
}
