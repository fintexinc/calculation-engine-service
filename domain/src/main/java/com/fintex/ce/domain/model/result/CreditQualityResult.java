package com.fintex.ce.domain.model.result;

import com.fintex.ce.domain.model.calculation.FixedIncomeCreditQuality;

import java.math.BigDecimal;
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
public class CreditQualityResult extends WarningResult {

  private Map<FixedIncomeCreditQuality, BigDecimal> creditQuality;
}
