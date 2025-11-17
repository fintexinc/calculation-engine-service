package com.fintex.ce.service.interfaces.calculation;

import com.fintex.ce.dto.request.TopCommonHoldingsReqDTO;
import com.fintex.ce.dto.response.TopCommonHoldingsResDTO;

public interface CommonHoldingsService {

    TopCommonHoldingsResDTO perform(final TopCommonHoldingsReqDTO reqDTO);
}
