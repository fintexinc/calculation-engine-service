package com.fintex.ce.model.domain.result.exposure;

import com.fintex.ce.model.domain.result.WarningResult;
import com.fintex.wm.commons.domain.rating.StyleBoxType;

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
public class EquityStyleboxExposureResult extends WarningResult {

  private Map<StyleBoxType, BigDecimal> equityStyleboxExposure;
}
