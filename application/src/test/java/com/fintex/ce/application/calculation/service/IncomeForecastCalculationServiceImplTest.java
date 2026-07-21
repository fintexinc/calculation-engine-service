package com.fintex.ce.application.calculation.service;

import com.fintex.ce.model.domain.calculation.distribution.Income;
import com.fintex.ce.model.domain.result.income.IncomeForecastResult;
import com.fintex.ce.model.dto.command.IncomeForecastCommand;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncomeForecastCalculationServiceImplTest {

  private final IncomeForecastCalculationServiceImpl service = new IncomeForecastCalculationServiceImpl();

  @Test
  void shouldTestPerform_whenConditionIsMet() {
    IncomeForecastCommand command = new IncomeForecastCommand();

    IncomeForecastResult response = service.perform(command, Map.of());

    assertNotNull(response);
    assertTrue(response.getIncomeForecast().isEmpty());
  }

  @Test
  void shouldTestCalculate_whenIncome() {
    BigDecimal dividendYield = new BigDecimal("0.05");
    List<String> dates = List.of("1-30", "3-15", "6-20", "10-12");
    BigDecimal amount = BigDecimal.TEN;
    int terms = 12;
    List<Income> incomes = service.calculateIncome(dividendYield, dates, amount, terms, Calendar.getInstance());
    assertEquals(4, incomes.size());
  }

  @Test
  void shouldTestCalculate_whenIncomeWithNoDividendDates() {
    BigDecimal dividendYield = new BigDecimal("0.05");
    List<String> dates = List.of();
    BigDecimal amount = BigDecimal.TEN;
    int terms = 12;
    List<Income> incomes = service.calculateIncome(dividendYield, dates, amount, terms, Calendar.getInstance());
    assertTrue(incomes.stream().allMatch(income -> income.getAmount().equals(BigDecimal.ZERO)));
  }

}
