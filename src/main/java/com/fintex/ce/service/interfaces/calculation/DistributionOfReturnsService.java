package com.fintex.ce.service.interfaces.calculation;

import com.fintex.ce.dto.request.DistributionOfReturnsReqDTO;
import com.fintex.ce.dto.response.distributionofreturns.DistributionOfReturnsResDTO;

public interface DistributionOfReturnsService {

    DistributionOfReturnsResDTO perform(final DistributionOfReturnsReqDTO reqDTO);

}
