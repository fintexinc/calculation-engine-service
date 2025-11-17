package com.fintex.ce.service.interfaces.calculation;

import com.fintex.ce.dto.request.DailyPerformanceReqDTO;
import com.fintex.ce.dto.response.InflationResDTO;

public interface InflationService {
    InflationResDTO perform(DailyPerformanceReqDTO reqDTO);
}
