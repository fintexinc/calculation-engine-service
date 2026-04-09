package com.fintex.ce.port.webclient;

import com.fintex.ce.domain.model.FxRates;

import java.time.LocalDate;
import java.util.Map;

public interface FxRatesFetcher {

  Map<LocalDate, FxRates.FxRate> fetch();

}
