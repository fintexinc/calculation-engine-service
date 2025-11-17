package com.fintex.ce.service.interfaces.calculation;

import com.fintex.ce.dto.request.IncomeForecastReqDTO;
import com.fintex.ce.dto.response.IncomeForecastResDto;


public interface IncomeForecastService {
    IncomeForecastResDto perform(final IncomeForecastReqDTO reqDTO);
}
