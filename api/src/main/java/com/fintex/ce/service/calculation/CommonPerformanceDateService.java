package com.fintex.ce.service.calculation;

import com.fintex.ce.port.input.command.MultiplePortfoliosCommand;
import com.fintex.ce.port.input.result.CommonPerformanceDatesResult;

public interface CommonPerformanceDateService {

  CommonPerformanceDatesResult commonPerformanceDate(final MultiplePortfoliosCommand command);

}
