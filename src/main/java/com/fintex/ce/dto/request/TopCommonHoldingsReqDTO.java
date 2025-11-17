package com.fintex.ce.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TopCommonHoldingsReqDTO extends PortfolioHoldingsReqDTO {

    private Integer numOfFundsMin;

    private Integer numOfTopCommonHoldings;

    private Set<String> accumulateHoldingTypes;

}
