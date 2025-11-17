package com.fintex.ce.service.interfaces.calculation;

import com.fintex.ce.dto.request.MultiplePortfoliosReqDTO;
import com.fintex.ce.dto.response.CommonPerformanceDatesResDTO;

public interface CommonPerformanceDateService {

    CommonPerformanceDatesResDTO commonPerformanceDate(final MultiplePortfoliosReqDTO mReqDTO);

}
