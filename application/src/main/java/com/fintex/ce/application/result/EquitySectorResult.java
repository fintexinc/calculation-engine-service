package com.fintex.ce.application.result;

import com.fintex.ce.domain.enumeration.calculation.EquitySectorAllocationType;
import com.fintex.ce.port.input.result.WarningResult;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class EquitySectorResult extends WarningResult {

  private Map<EquitySectorAllocationType, BigDecimal> equitySector;
}
