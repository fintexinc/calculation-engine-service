package com.fintex.ce.adapter.rest.dto.request;

import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.adapter.rest.dto.request.core.DataProviderReqDTO;
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
