package com.fintex.ce.port.output;

import com.fintex.ce.domain.model.FxRates;

import java.time.LocalDate;
import java.util.Map;

public interface FxRatesPort {

  Map<LocalDate, FxRates.FxRate> loadFxRates();

}
