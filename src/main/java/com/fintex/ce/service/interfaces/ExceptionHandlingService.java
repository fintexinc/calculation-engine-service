package com.fintex.ce.service.interfaces;

import com.fintex.ce.dto.response.core.ErrorDTO;
import com.fintex.ce.exception.DataErrorException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.function.Supplier;

public interface ExceptionHandlingService {

    void removeRedisCacheForRequestedHoldings(final HttpServletRequest request, final Exception e, final String requestUri);

    void removeFxRatesFromRedisCache();

    void ifFxRatesErrorRemoveFxRatesFromRedisCache(DataErrorException e);

    <T extends ErrorDTO> T returnObjectWithListOfErrors(Supplier<T> function, Supplier<T> function2, HttpServletRequest request);
}
