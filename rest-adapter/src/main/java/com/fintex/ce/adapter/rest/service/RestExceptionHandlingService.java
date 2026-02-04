package com.fintex.ce.adapter.rest.service;

import com.fintex.ce.adapter.rest.dto.response.core.ErrorDTO;
import com.fintex.ce.service.ExceptionHandlingService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.function.Supplier;

public interface RestExceptionHandlingService extends ExceptionHandlingService {

  void removeRedisCacheForRequestedHoldings(final HttpServletRequest request, final Exception e,
      final String requestUri);

  <T extends ErrorDTO> T returnObjectWithListOfErrors(Supplier<T> function, Supplier<T> function2,
      HttpServletRequest request);
}
