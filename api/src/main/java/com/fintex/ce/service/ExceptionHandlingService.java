package com.fintex.ce.service;

import com.fintex.ce.domain.exception.DataErrorException;

public interface ExceptionHandlingService {

  void removeFxRatesFromRedisCache();

  void ifFxRatesErrorRemoveFxRatesFromRedisCache(DataErrorException e);
}
