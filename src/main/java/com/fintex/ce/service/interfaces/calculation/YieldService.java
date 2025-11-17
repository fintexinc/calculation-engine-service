package com.fintex.ce.service.interfaces.calculation;

import com.fintex.ce.dto.request.YieldReqDTO;
import com.fintex.ce.dto.response.YieldResDto;

public interface YieldService {

    YieldResDto perform(final YieldReqDTO reqDTO);

}
