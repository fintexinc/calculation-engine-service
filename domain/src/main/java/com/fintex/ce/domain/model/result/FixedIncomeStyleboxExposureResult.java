package com.fintex.ce.domain.model.result;

import com.fintex.sm.model.domain.enumeration.FixedIncomeStyleBoxType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class FixedIncomeStyleboxExposureResult extends WarningResult {

  private Map<FixedIncomeStyleBoxType, BigDecimal> fixedIncomeStyleboxExposure;
}
