package com.fintex.ce.port.input.result;

import com.fintex.ce.domain.enumeration.calculation.EquityMarketCapType;
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
public class EquityMarketCapResult extends WarningResult {

  private Map<EquityMarketCapType, BigDecimal> equityMarketCapitalization;
}
