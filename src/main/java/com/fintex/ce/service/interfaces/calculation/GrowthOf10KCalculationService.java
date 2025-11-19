package com.fintex.ce.service.interfaces.calculation;

import com.fintex.ce.dto.request.ReturnReqDTO;
import com.fintex.ce.dto.response.Growth10KResDTO;

public interface GrowthOf10KCalculationService {

    Growth10KResDTO perform(final ReturnReqDTO reqDTO);

}
