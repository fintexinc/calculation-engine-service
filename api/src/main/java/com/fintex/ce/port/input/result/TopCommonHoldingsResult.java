package com.fintex.ce.port.input.result;

import com.fintex.ce.port.input.result.commonholdings.TopCommonHoldingData;
import com.fintex.ce.port.input.result.WarningResult;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class TopCommonHoldingsResult extends WarningResult {

  private List<TopCommonHoldingData> commonHoldings;
}
