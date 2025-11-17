package com.fintex.ce.service.interfaces.calculation;

import com.fintex.ce.dto.request.BestWorstPeriodsReqDTO;
import com.fintex.ce.dto.response.BestWorstPeriodsResponseDTO;

public interface BestWorstPeriodsCalculationService {

    BestWorstPeriodsResponseDTO perform(final BestWorstPeriodsReqDTO reqDTO);

}
