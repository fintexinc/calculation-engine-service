package com.fintex.ce.service.interfaces;

import com.fintex.ce.dto.request.DailyPerformanceReqDTO;
import com.fintex.ce.dto.response.DistributionResDTO;

public interface DistributionService {

    DistributionResDTO perform(final DailyPerformanceReqDTO reqDTO);
}
