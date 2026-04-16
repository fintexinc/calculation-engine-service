package com.fintex.ce.model.domain.result.fee;

import com.fintex.ce.model.domain.enumeration.ParameterType;
import com.fintex.ce.model.domain.result.WarningResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class ManagementFeeResult extends WarningResult {

  private Map<ParameterType, BigDecimal> managementFee = new HashMap<>();
}
