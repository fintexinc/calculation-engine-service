package com.fintex.ce.service.interfaces.calculation;

import com.fintex.ce.dto.request.ReturnReqDTO;
import com.fintex.ce.dto.response.AnnualReturnResDTO;

public interface AnnualReturnService {

    AnnualReturnResDTO<Integer> perform(final ReturnReqDTO reqDTO);

}
