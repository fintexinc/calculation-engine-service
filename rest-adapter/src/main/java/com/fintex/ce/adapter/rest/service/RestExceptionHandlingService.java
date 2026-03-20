package com.fintex.ce.adapter.rest.service;

import com.fintex.ce.adapter.rest.dto.response.core.ErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.function.Supplier;

public interface RestExceptionHandlingService {

  <T extends ErrorDTO> T returnObjectWithListOfErrors(Supplier<T> function, Supplier<T> function2,
      HttpServletRequest request);
}
