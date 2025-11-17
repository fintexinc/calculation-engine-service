package com.fintex.ce.dto.request;

import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.core.DataProviderReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PortfolioHoldingsReqDTO extends DataProviderReqDTO {

    private List<Holding> holdings;

}
