package com.fintex.ce.domain.model.result;

import com.fintex.sm.model.domain.enumeration.StyleBoxType;
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
public class EquityStyleboxExposureResult extends WarningResult {

  private Map<StyleBoxType, BigDecimal> equityStyleboxExposure;
}
