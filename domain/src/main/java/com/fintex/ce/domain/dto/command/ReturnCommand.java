package com.fintex.ce.domain.dto.command;

import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.holding.Holding;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ReturnCommand implements CalculationCommand {
  private LocalDate customPerformanceStartDate;
  private LocalDate customPerformanceEndDate;
  private Currency currency;
  private List<Holding> holdings;
}
