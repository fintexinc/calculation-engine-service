package com.fintex.ce.service.interfaces.calculation;

import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.SalesChargeResDtos;

public interface SalesChargeService {

    SalesChargeResDtos perform(final PortfolioHoldingsReqDTO reqDTO);

}
