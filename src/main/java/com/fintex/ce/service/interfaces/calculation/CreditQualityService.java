package com.fintex.ce.service.interfaces.calculation;

import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.CreditQualityResDTO;

public interface CreditQualityService {

    CreditQualityResDTO perform(final PortfolioHoldingsReqDTO reqDTO);

}
