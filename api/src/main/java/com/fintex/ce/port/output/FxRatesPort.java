package com.fintex.ce.port.output;

import com.fintex.smclient.dto.FxRatesDTO;

import java.time.LocalDate;
import java.util.Map;

public interface FxRatesPort {

  Map<LocalDate, FxRatesDTO> loadFxRates();

}
