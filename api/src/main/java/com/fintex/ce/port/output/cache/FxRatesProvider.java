package com.fintex.ce.port.output.cache;

import com.fintex.smclient.dto.FxRatesDTO;

import java.time.LocalDate;
import java.util.Map;

public interface FxRatesProvider {

  Map<LocalDate, FxRatesDTO> loadFxRates();

}
