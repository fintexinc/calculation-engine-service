package com.fintex.ce.domain.model.result;

import com.fintex.sm.model.domain.enumeration.EquityMarketCapitalizationType;

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
public class EquityMarketCapResult extends WarningResult {

  private Map<EquityMarketCapitalizationType, BigDecimal> equityMarketCapitalization;
}
