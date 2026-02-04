package com.fintex.ce.application.command;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.model.holding.Holding;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

@Data
@Accessors(chain = true)
public class ReturnCommand {
  private LocalDate customPerformanceStartDate;
  private LocalDate customPerformanceEndDate;
  private Currency currency;
  private List<Holding> holdings;
}
