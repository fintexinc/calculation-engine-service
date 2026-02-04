package com.fintex.ce.application.result;

import com.fintex.ce.port.input.result.WarningResult;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class YieldResult extends WarningResult {

  private BigDecimal yield;
}
