package com.fintex.ce.service.calculation;

import com.fintex.ce.domain.dto.command.MultiplePortfoliosCommand;
import com.fintex.ce.domain.model.result.CommonPerformanceDatesResult;

public interface CommonPerformanceDateService {

  CommonPerformanceDatesResult commonPerformanceDate(final MultiplePortfoliosCommand command);

}
