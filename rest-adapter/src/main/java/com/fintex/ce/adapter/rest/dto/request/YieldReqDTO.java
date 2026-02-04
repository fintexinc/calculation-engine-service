package com.fintex.ce.adapter.rest.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class YieldReqDTO extends PortfolioHoldingsReqDTO {

  private Integer timeIntervalPeriods;

}
