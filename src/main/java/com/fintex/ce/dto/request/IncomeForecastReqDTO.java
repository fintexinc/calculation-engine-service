package com.fintex.ce.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class IncomeForecastReqDTO extends PortfolioHoldingsReqDTO {

    private Integer timeIntervalPeriods;

}
