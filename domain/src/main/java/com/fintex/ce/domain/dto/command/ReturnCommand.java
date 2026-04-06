package com.fintex.ce.domain.dto.command;

import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

@Data
@Accessors(chain = true)
public class ReturnCommand implements CalculationCommand {
  private LocalDate customPerformanceStartDate;
  private LocalDate customPerformanceEndDate;
  private CurrencyType currency;
  private List<Holding> holdings;
}
